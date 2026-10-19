package dev.iptvplayer.tv;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.view.FrameMetrics;
import android.view.Window;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Test-only framework runner: no class links to the R8 app, Kotlin or AndroidX test runtime. */
@android.annotation.TargetApi(34)
public final class ReleaseStartupInstrumentation extends Instrumentation implements Application.ActivityLifecycleCallbacks {
    private static final String ACTIVITY = "dev.iptvplayer.tv.MainActivity";
    private final ConcurrentLinkedQueue<Sample> frames = new ConcurrentLinkedQueue<>();
    private final CompletableFuture<Sample> first = new CompletableFuture<>();
    private HandlerThread worker;
    private Window window;
    private Bundle arguments;
    private long preCreate;
    private int activities;
    private final Window.OnFrameMetricsAvailableListener listener = (win, metrics, dropped) -> {
        Sample frame = new Sample(metrics, dropped);
        frames.add(frame); first.complete(frame);
    };
    private static final class Sample {
        final FrameMetrics metrics;
        final long received = System.nanoTime();
        final int dropped;
        Sample(FrameMetrics metrics, int dropped) { this.metrics = new FrameMetrics(metrics); this.dropped = dropped; }
    }
    @Override public void onCreate(Bundle args) { super.onCreate(args); arguments = args; start(); }
    private static void require(boolean value, String message) { if (!value) throw new IllegalStateException(message); }
    private static double ms(long value) { return value / 1_000_000.0; }

    @Override public void onStart() {
        Activity activity = null;
        Application application = null;
        ExecutorService screenshots = Executors.newSingleThreadExecutor();
        Bundle result = new Bundle();
        boolean passed = false;
        try {
            require(android.os.Build.VERSION.SDK_INT >= 34, "API34 required");
            require("true".equals(arguments.getString("verifyReleaseCatalog")), "Explicit release mode required");
            require(android.os.Build.HARDWARE.contains("ranchu") || android.os.Build.FINGERPRINT.contains("generic"), "Test emulator required");
            boolean expectedDebug = "true".equals(arguments.getString("expectedDebug"));
            require(((getTargetContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) == expectedDebug, "Unexpected APK build type");
            require(android.animation.ValueAnimator.areAnimatorsEnabled(), "Animations must be enabled");
            File seed = new File(getTargetContext().getFilesDir(), "cold-seed-pid.txt");
            int seedPid = Integer.parseInt(new String(Files.readAllBytes(seed.toPath()), StandardCharsets.UTF_8).trim());
            require(seedPid != android.os.Process.myPid(), "A new process is required");
            worker = new HandlerThread("ReleaseStartupFrames"); worker.start();
            application = (Application) getTargetContext().getApplicationContext();
            Application observed = application;
            runOnMainSync(() -> observed.registerActivityLifecycleCallbacks(this));
            boolean visual = "true".equals(arguments.getString("captureColdEntrance"));
            if (visual) getUiAutomation(); // Connect before the measured launch.
            CompletableFuture<JSONArray> captures = visual ? capture(screenshots) : null;
            long launch = System.nanoTime();
            activity = startActivitySync(new Intent(Intent.ACTION_MAIN).setClassName(getTargetContext().getPackageName(), ACTIVITY)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            Sample beginning = first.get(15, TimeUnit.SECONDS);
            long remaining = 6000 - (System.nanoTime() - beginning.received) / 1_000_000;
            if (remaining > 0) SystemClock.sleep(remaining);
            JSONArray entrance = captures == null ? null : captures.get(15, TimeUnit.SECONDS);
            List<Sample> snapshot = new ArrayList<>(frames);
            JSONObject timing = report(snapshot, launch);
            JSONObject record = new JSONObject().put("release", !expectedDebug).put("version",
                getTargetContext().getPackageManager().getPackageInfo(getTargetContext().getPackageName(), 0).versionName)
                .put("seedPid", seedPid).put("verifyPid", android.os.Process.myPid()).put("frameMetrics", timing)
                .put("visualSampling", visual).put("scope", "Actual Activity, framework-only instrumentation, synthetic silent catalog. Application setup and physical presentation excluded; no app-internal restore assertions. APK build type is reported separately.");
            if (entrance != null) record.put("entranceSamples", entrance);
            Files.write(new File(outputDirectory(), "release-cold-verification.json").toPath(), record.toString(2).getBytes(StandardCharsets.UTF_8));
            require(activities == 1 && timing.getInt("initialFrameFlag") == 1, "Initial Activity frame required");
            require(snapshot.size() >= 10 && timing.getDouble("vsyncSpanMs") >= 2800, "Incomplete rendered timeline");
            require(timing.getInt("droppedMetricReports") == 0, "Dropped metric callbacks invalidate comparison");
            passed = true;
            result.putString("stream", "\nOK (1 test)\n");
        } catch (Throwable failure) {
            result.putString("stream", "\nFAILURES!!!\n" + android.util.Log.getStackTraceString(failure));
        } finally {
            Activity shown = activity; Application observed = application;
            if (observed != null) runOnMainSync(() -> {
                observed.unregisterActivityLifecycleCallbacks(this);
                if (window != null) window.removeOnFrameMetricsAvailableListener(listener);
                if (shown != null) shown.finish();
            });
            screenshots.shutdownNow();
            if (worker != null) { worker.quitSafely(); try { worker.join(2000); } catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); } }
        }
        Bundle status = new Bundle(); status.putString("class", getClass().getName()); status.putString("test", "actualReleaseActivity");
        sendStatus(passed ? 0 : -2, status);
        finish(passed ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
    }

    private JSONObject report(List<Sample> values, long launch) throws Exception {
        Sample initial = values.get(0); long origin = initial.metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP);
        List<Long> totals = new ArrayList<>(); List<Long> cadence = new ArrayList<>();
        cadence.add(origin); cadence.add(origin + 2_800_000_000L);
        JSONArray raw = new JSONArray(); int dropped = 0;
        for (Sample value : values) {
            FrameMetrics frame = value.metrics; long vsync = frame.getMetric(FrameMetrics.VSYNC_TIMESTAMP);
            if (vsync > origin && vsync < origin + 2_800_000_000L) cadence.add(vsync);
            if (frame.getMetric(FrameMetrics.FIRST_DRAW_FRAME) == 0 && vsync - origin <= 2_800_000_000L)
                totals.add(frame.getMetric(FrameMetrics.TOTAL_DURATION));
            dropped += value.dropped;
            raw.put(new JSONObject().put("vsyncNs", vsync).put("first", frame.getMetric(FrameMetrics.FIRST_DRAW_FRAME))
                .put("totalNs", frame.getMetric(FrameMetrics.TOTAL_DURATION)).put("unknownDelayNs", frame.getMetric(FrameMetrics.UNKNOWN_DELAY_DURATION))
                .put("animationNs", frame.getMetric(FrameMetrics.ANIMATION_DURATION)).put("layoutMeasureNs", frame.getMetric(FrameMetrics.LAYOUT_MEASURE_DURATION))
                .put("drawNs", frame.getMetric(FrameMetrics.DRAW_DURATION)).put("syncNs", frame.getMetric(FrameMetrics.SYNC_DURATION))
                .put("commandIssueNs", frame.getMetric(FrameMetrics.COMMAND_ISSUE_DURATION)).put("swapBuffersNs", frame.getMetric(FrameMetrics.SWAP_BUFFERS_DURATION))
                .put("gpuNs", frame.getMetric(FrameMetrics.GPU_DURATION)).put("droppedReports", value.dropped));
        }
        Collections.sort(cadence); Collections.sort(totals); long gap = 0;
        for (int i = 1; i < cadence.size(); i++) gap = Math.max(gap, cadence.get(i) - cadence.get(i - 1));
        return new JSONObject().put("activityCount", activities).put("frames", values.size()).put("initialFrameFlag", initial.metrics.getMetric(FrameMetrics.FIRST_DRAW_FRAME))
            .put("vsyncSpanMs", ms(values.get(values.size() - 1).metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP) - origin))
            .put("droppedMetricReports", dropped).put("launchToFirstCallbackMs", ms(initial.received - launch))
            .put("preCreateToFirstCallbackMs", ms(initial.received - preCreate)).put("firstFrameTotalMs", ms(initial.metrics.getMetric(FrameMetrics.TOTAL_DURATION)))
            .put("introFrames", totals.size()).put("introVsyncMaxGapMs", ms(gap))
            .put("introTotalP95Ms", totals.isEmpty() ? 0 : ms(totals.get((int) ((totals.size() - 1) * .95)))).put("rawFrames", raw);
    }

    private CompletableFuture<JSONArray> capture(ExecutorService executor) {
        CompletableFuture<JSONArray> done = new CompletableFuture<>();
        executor.execute(() -> {
            List<Bitmap> images = new ArrayList<>(); JSONArray samples = new JSONArray();
            try {
                long origin = first.get(15, TimeUnit.SECONDS).metrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP);
                for (long target : new long[] {2000, 2600, 3000, 3300, 3600, 4200}) {
                    long wait = target - (System.nanoTime() - origin) / 1_000_000;
                    if (wait > 0) SystemClock.sleep(wait);
                    long requested = System.nanoTime(); Bitmap bitmap = getUiAutomation().takeScreenshot(window); long completed = System.nanoTime();
                    require(bitmap != null, "Window screenshot unavailable"); images.add(bitmap);
                    samples.put(new JSONObject().put("targetMs", target).put("requestedMs", ms(requested - origin)).put("completedMs", ms(completed - origin)));
                }
                File directory = new File(outputDirectory(), "release-cold-entrance");
                require(directory.isDirectory() || directory.mkdirs(), "Cannot save synthetic captures");
                for (int i = 0; i < images.size(); i++) try (FileOutputStream file = new FileOutputStream(new File(directory, "frame-" + i + ".png"))) {
                    images.get(i).compress(Bitmap.CompressFormat.PNG, 100, file);
                }
                done.complete(samples);
            } catch (Throwable failure) { done.completeExceptionally(failure); }
            finally { for (Bitmap bitmap : images) bitmap.recycle(); }
        });
        return done;
    }

    private File outputDirectory() {
        // Instrumentation runs as the target UID; the test APK's private files belong to another UID.
        File output = getTargetContext().getExternalFilesDir(null);
        require(output != null && (output.isDirectory() || output.mkdirs()), "Synthetic output directory unavailable");
        return output;
    }

    @Override public void onActivityPreCreated(Activity activity, Bundle state) {
        if (!ACTIVITY.equals(activity.getComponentName().getClassName())) return;
        require(window == null, "One initial Activity only"); activities++; preCreate = System.nanoTime(); window = activity.getWindow();
        window.addOnFrameMetricsAvailableListener(listener, new Handler(worker.getLooper()));
    }
    @Override public void onActivityCreated(Activity a, Bundle s) { }
    @Override public void onActivityStarted(Activity a) { }
    @Override public void onActivityResumed(Activity a) { }
    @Override public void onActivityPaused(Activity a) { }
    @Override public void onActivityStopped(Activity a) { }
    @Override public void onActivitySaveInstanceState(Activity a, Bundle s) { }
    @Override public void onActivityDestroyed(Activity a) { }
}
