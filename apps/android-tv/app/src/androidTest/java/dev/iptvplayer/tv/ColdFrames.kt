package dev.iptvplayer.tv

import android.app.Activity
import android.app.Application
import android.app.Instrumentation
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.view.FrameMetrics
import android.view.Window
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Test-only observer attached before Activity.onCreate; never asks Compose to idle or advances its clock. */
@androidx.annotation.RequiresApi(29)
internal class ColdFrames(private val instrumentation: Instrumentation) : Application.ActivityLifecycleCallbacks, AutoCloseable {
    private data class Frame(val received: Long, val dropped: Int, val metrics: FrameMetrics)
    private val app = instrumentation.targetContext.applicationContext as Application
    private val thread = HandlerThread("ColdFrames").apply { start() }
    private val handler = Handler(thread.looper)
    private val frames = ConcurrentLinkedQueue<Frame>()
    private val states = ConcurrentLinkedQueue<Pair<Long, String>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val first = CompletableFuture<Frame>()
    @Volatile private var window: Window? = null
    @Volatile private var preCreateNs = 0L
    @Volatile private var activityCount = 0
    private val listener = Window.OnFrameMetricsAvailableListener { _, value, dropped ->
        val frame = Frame(System.nanoTime(), dropped, FrameMetrics(value))
        frames.add(frame); first.complete(frame)
    }
    fun register() = instrumentation.runOnMainSync { app.registerActivityLifecycleCallbacks(this) }
    override fun onActivityPreCreated(activity: Activity, state: Bundle?) {
        if (activity !is MainActivity) return
        activityCount++
        check(window == null)
        preCreateNs = System.nanoTime()
        window = activity.window
        activity.window.addOnFrameMetricsAvailableListener(listener, handler)
    }
    override fun onActivityPostResumed(activity: Activity) {
        if (activity !is MainActivity) return
        val model = ViewModelProvider(activity)[TvViewModel::class.java]
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            model.state.map { it.sourceTask }.distinctUntilChanged().collect { states.add(System.nanoTime() to it.name) }
        }
    }
    fun waitForObservation() {
        val beginning = first.get(15, TimeUnit.SECONDS).received
        val remaining = 6_000 - (System.nanoTime() - beginning) / 1_000_000
        if (remaining > 0) SystemClock.sleep(remaining)
    }
    fun report(launchNs: Long): JSONObject {
        val recorded = frames.toList()
        val initial = recorded.first()
        val firstVsync = initial.metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP)
        val intro = recorded.filter {
            it.metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 0L &&
                it.metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP) - firstVsync in 0..2_800_000_000L
        }
        val totals = intro.map { it.metrics.getMetric(FrameMetrics.TOTAL_DURATION) }.sorted()
        val cadence = (listOf(firstVsync, firstVsync + 2_800_000_000L) + recorded.map {
            it.metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP)
        }.filter { it > firstVsync && it < firstVsync + 2_800_000_000L }).distinct().sorted()
        val intervals = cadence.zipWithNext { a, b -> b - a }
        fun ms(n: Long) = n / 1_000_000.0
        return JSONObject().put("activityCount", activityCount).put("frames", recorded.size)
            .put("initialFrameFlag", initial.metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME))
            .put("vsyncSpanMs", ms(recorded.last().metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP) - firstVsync))
            .put("firstDrawFlags", recorded.count { it.metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 1L })
            .put("droppedMetricReports", recorded.sumOf { it.dropped })
            .put("launchToFirstCallbackMs", ms(initial.received - launchNs))
            .put("preCreateToFirstCallbackMs", ms(initial.received - preCreateNs))
            .put("firstFrameTotalMs", ms(initial.metrics.getMetric(FrameMetrics.TOTAL_DURATION)))
            .put("observationSpanMs", ms(recorded.last().received - initial.received))
            .put("introFrames", intro.size).put("introVsyncMaxGapMs", ms(intervals.maxOrNull() ?: 0))
            .put("introTotalP95Ms", ms(totals.getOrElse(((totals.size - 1) * .95).toInt().coerceAtLeast(0)) { 0 }))
            .put("introTotalMaxMs", ms(totals.lastOrNull() ?: 0))
            .put("restoreStates", JSONArray(states.map { (time, value) ->
                JSONObject().put("afterPreCreateMs", ms(time - preCreateNs)).put("state", value)
            }))
            .put("rawFrames", JSONArray(recorded.map { frame ->
                JSONObject().put("receivedAfterPreCreateMs", ms(frame.received - preCreateNs))
                    .put("vsyncNs", frame.metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP))
                    .put("intendedVsyncNs", frame.metrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP))
                    .put("first", frame.metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME))
                    .put("totalNs", frame.metrics.getMetric(FrameMetrics.TOTAL_DURATION))
                    .put("unknownDelayNs", frame.metrics.getMetric(FrameMetrics.UNKNOWN_DELAY_DURATION))
                    .put("droppedReports", frame.dropped)
            }))
            .put("scope", "New instrumented process, actual MainActivity creation and real clock. Application/instrumentation setup and physical display presentation are outside this measurement. No fixed FPS pass threshold.")
    }
    /** Separate visual run only: screen-copy overhead must not pollute the timing-only comparison. */
    @androidx.annotation.RequiresApi(34)
    fun sampleEntrance(): CompletableFuture<JSONArray> {
        val done = CompletableFuture<JSONArray>()
        val automation = instrumentation.uiAutomation
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val captured = mutableListOf<Pair<JSONObject, Bitmap>>()
            try {
                val origin = first.get(15, TimeUnit.SECONDS).metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP)
                // A slow first draw can consume the early samples before the logo starts. Keep late samples too.
                for (target in listOf(400L, 1200L, 2000L, 2800L, 3600L)) {
                    val wait = target - (System.nanoTime() - origin) / 1_000_000
                    if (wait > 0) SystemClock.sleep(wait)
                    val requested = System.nanoTime()
                    val bitmap = checkNotNull(automation.takeScreenshot(checkNotNull(window)))
                    val completed = System.nanoTime()
                    val left = bitmap.width * 40 / 100; val top = bitmap.height * 33 / 100
                    val width = bitmap.width * 60 / 100 - left; val height = bitmap.height * 67 / 100 - top
                    val pixels = IntArray(width * height)
                    bitmap.getPixels(pixels, 0, width, left, top, width, height)
                    val logoPixels = pixels.count { (it shr 8 and 255) > 160 }
                    captured += JSONObject().put("targetMs", target).put("requestedMs", (requested-origin)/1_000_000.0)
                        .put("completedMs", (completed-origin)/1_000_000.0).put("logoPixels", logoPixels) to bitmap
                }
                val directory = instrumentation.targetContext.filesDir.resolve("cold-entrance").apply { mkdirs() }
                captured.forEachIndexed { index, (_, bitmap) ->
                    directory.resolve("frame-$index.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                }
                done.complete(JSONArray(captured.map { it.first }))
            } catch (e: Throwable) { done.completeExceptionally(e) }
            finally { captured.forEach { it.second.recycle() }; executor.shutdown() }
        }
        return done
    }
    override fun close() {
        scope.cancel()
        try {
            instrumentation.runOnMainSync {
                app.unregisterActivityLifecycleCallbacks(this)
                window?.removeOnFrameMetricsAvailableListener(listener)
            }
            val drained = CountDownLatch(1); handler.post { drained.countDown() }
            check(drained.await(2, TimeUnit.SECONDS))
        } finally {
            thread.quitSafely(); thread.join(2000)
        }
    }
    override fun onActivityCreated(a: Activity, s: Bundle?) {}
    override fun onActivityStarted(a: Activity) {}
    override fun onActivityResumed(a: Activity) {}
    override fun onActivityPaused(a: Activity) {}
    override fun onActivityStopped(a: Activity) {}
    override fun onActivitySaveInstanceState(a: Activity, s: Bundle) {}
    override fun onActivityDestroyed(a: Activity) {}
}
