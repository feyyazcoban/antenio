package dev.iptvplayer.tv.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.AtomicFile
import java.io.Closeable
import java.io.File
import java.util.UUID

data class EpgSnapshotInfo(val updatedAt: Long, val window: GuideWindow, val channels: Int,
    val programmes: Int, val invalid: Int, val outsideWindow: Int)

/** App-private indexed guide metadata, without feed/stream URLs or account credentials.
 * Network parsing writes an isolated staging database; only a closed, complete file is published.
 * All instances share publication/removal locking. Call on IO, outside Compose rendering. */
class EpgStore(context: Context) {
    private val directory = File(context.filesDir, "guides")
    private val staging = File(context.cacheDir, "guide-staging")
    private fun file(plan: EpgPlan) = File(directory, "${plan.owner}-${plan.binding}.db")
    private fun generationKey(owner: String) = directory.absolutePath + ":" + owner

    fun stage(plan: EpgPlan, window: GuideWindow): Stage = synchronized(lock) {
        check(staging.isDirectory || staging.mkdirs())
        // Abandoned stages after process death are disposable. Never remove another live writer.
        staging.listFiles().orEmpty().filter { it.isFile && Regex("[a-f0-9-]+\\.db(?:-journal|-wal|-shm)?").matches(it.name) }
            .map { File(staging, it.name.substringBefore(".db") + ".db") }.distinct()
            .filter { it.absolutePath !in activeFiles }.forEach(::deleteDatabaseFiles)
        val key = generationKey(plan.owner)
        val generation = (generations[key] ?: 0L) + 1
        generations[key] = generation
        val path = File(staging, "${UUID.randomUUID()}.db")
        activeFiles += path.absolutePath
        try { Stage(plan, window, path, key, generation) }
        catch (e: Exception) { activeFiles -= path.absolutePath; deleteDatabaseFiles(path); throw e }
    }

    /** False means a newer request/removal superseded this stage. The old snapshot stays intact. */
    fun publish(stage: Stage): Boolean = synchronized(lock) {
        check(stage.sealed && !stage.closed)
        check(stage.key == generationKey(stage.plan.owner))
        if (generations[stage.key] != stage.generation) return@synchronized false
        check(directory.isDirectory || directory.mkdirs())
        val atomic = AtomicFile(file(stage.plan))
        val output = atomic.startWrite()
        try { stage.path.inputStream().use { it.copyTo(output) }; atomic.finishWrite(output) }
        catch (e: Exception) { atomic.failWrite(output); throw e }
        // Discard older account/feed bindings only after the new file is durably in place.
        directory.listFiles().orEmpty().filter { it.isFile && it.name.startsWith(stage.plan.owner + "-") && it != file(stage.plan) }
            .forEach { it.delete() }
        true
    }

    fun info(plan: EpgPlan): EpgSnapshotInfo? = read(plan) { db ->
        db.rawQuery("SELECT updated, from_time, until_time, channels, programmes, invalid, outside FROM snapshot", null).use { c ->
            if (!c.moveToFirst()) null else EpgSnapshotInfo(c.getLong(0), GuideWindow(c.getLong(1), c.getLong(2)),
                c.getInt(3), c.getInt(4), c.getInt(5), c.getInt(6))
        }
    }

    fun query(plan: EpgPlan, entry: MediaEntry, window: GuideWindow, limit: Int = 512): GuideSlice? = read(plan) { db ->
        query(db, plan, entry, window, limit)
    }

    fun queryMany(plan: EpgPlan, entries: List<MediaEntry>, window: GuideWindow): Map<String, GuideSlice> =
        read(plan) { db -> entries.associate { it.id to query(db, plan, it, window, 512) } }.orEmpty()

    private fun query(db: SQLiteDatabase, plan: EpgPlan, entry: MediaEntry, window: GuideWindow, limit: Int): GuideSlice {
        require(limit in 1..2_000)
        if (entry.sourceId.isNotEmpty() && stableId(entry.sourceId) != plan.owner) return GuideSlice(EpgMatch(EpgMatchKind.NONE), emptyList())
        val match = match(db, entry)
        val ref = match.channel ?: return GuideSlice(match, emptyList())
        val result = mutableListOf<EpgProgramme>()
        db.rawQuery("""SELECT title, start, stop, description, category FROM programmes
            WHERE feed=? AND channel=? AND start<? AND (stop>? OR (stop=-1 AND start>=?))
            ORDER BY start, stop, title LIMIT ?""".trimIndent(),
            arrayOf(ref.feed.toString(), ref.id, window.until.toString(), window.from.toString(), window.from.toString(), (limit + 1).toString())).use { c ->
            while (c.moveToNext()) result += EpgProgramme(ref.id, c.getString(0), c.getLong(1),
                c.getLong(2).takeIf { it != -1L }, c.getString(3), c.getString(4))
        }
        return GuideSlice(match, result.take(limit), result.size > limit)
    }

    private fun match(db: SQLiteDatabase, entry: MediaEntry): EpgMatch {
        if (entry.kind != MediaKind.LIVE) return EpgMatch(EpgMatchKind.NONE)
        fun candidates(sql: String, args: Array<String>) = db.rawQuery(sql, args).use { c ->
            buildSet { while (c.moveToNext()) add(EpgChannelRef(c.getInt(0), c.getString(1))) }
        }
        if (entry.epgId.isNotBlank()) {
            val found = candidates("SELECT feed, id FROM channels WHERE id=? LIMIT 2", arrayOf(entry.epgId))
            return when (found.size) {
                1 -> EpgMatch(EpgMatchKind.ID, found.single())
                0 -> EpgMatch(EpgMatchKind.MISSING_ID)
                else -> EpgMatch(EpgMatchKind.AMBIGUOUS)
            }
        }
        val found = listOf(entry.epgName, entry.title).filter { it.isNotBlank() }.map(::epgNameKey).distinct()
            .flatMap { name -> candidates("SELECT feed, channel FROM aliases WHERE name=? LIMIT 2", arrayOf(name)) }.toSet()
        return when (found.size) {
            1 -> EpgMatch(EpgMatchKind.NAME, found.single())
            0 -> EpgMatch(EpgMatchKind.NONE)
            else -> EpgMatch(EpgMatchKind.AMBIGUOUS)
        }
    }

    private fun <T> read(plan: EpgPlan, block: (SQLiteDatabase) -> T): T? = synchronized(lock) {
        try {
            // Let AtomicFile recover a replacement interrupted before finishWrite.
            AtomicFile(file(plan)).openRead().close()
            SQLiteDatabase.openDatabase(file(plan).path, null, SQLiteDatabase.OPEN_READONLY).use(block)
        } catch (_: Exception) { null }
    }

    fun remove(sourceId: String) = synchronized(lock) {
        val owner = stableId(sourceId)
        val key = generationKey(owner)
        generations[key] = (generations[key] ?: 0L) + 1
        directory.listFiles().orEmpty().filter { it.isFile && it.name.startsWith("$owner-") }
            .forEach { check(it.delete() || !it.exists()) }
    }

    fun clear() = synchronized(lock) {
        generations.keys.filter { it.startsWith(directory.absolutePath + ":") }.forEach { generations[it] = generations.getValue(it) + 1 }
        directory.listFiles().orEmpty().filter { it.isFile }.forEach { check(it.delete() || !it.exists()) }
    }

    class Stage internal constructor(internal val plan: EpgPlan, private val window: GuideWindow,
        internal val path: File, internal val key: String, internal val generation: Long) : Closeable {
        private val writerThread = Thread.currentThread()
        private fun checkWriter() = check(Thread.currentThread() === writerThread) { "Guide transaction must stay on its writer thread" }
        internal var sealed = false; private set
        internal var closed = false; private set
        private val db = SQLiteDatabase.openOrCreateDatabase(path, null)
        private var inserted = 0
        init {
            try {
                db.rawQuery("PRAGMA journal_mode=DELETE", null).use { check(it.moveToFirst() && it.getString(0) == "delete") }
                val pageSize = db.rawQuery("PRAGMA page_size", null).use { it.moveToFirst(); it.getLong(0) }
                db.rawQuery("PRAGMA max_page_count=${96L * 1024 * 1024 / pageSize}", null).use { check(it.moveToFirst()) }
                db.execSQL("CREATE TABLE channels (feed INTEGER NOT NULL, id TEXT NOT NULL, PRIMARY KEY(feed,id))")
                db.execSQL("CREATE INDEX channel_id ON channels(id)")
                db.execSQL("CREATE TABLE aliases (name TEXT NOT NULL, feed INTEGER NOT NULL, channel TEXT NOT NULL, PRIMARY KEY(name,feed,channel))")
                db.execSQL("""CREATE TABLE programmes (feed INTEGER NOT NULL, channel TEXT NOT NULL, start INTEGER NOT NULL,
                    stop INTEGER NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, category TEXT NOT NULL,
                    PRIMARY KEY(feed,channel,start,stop,title))""".trimIndent())
                db.execSQL("CREATE TABLE snapshot (updated INTEGER, from_time INTEGER, until_time INTEGER, channels INTEGER, programmes INTEGER, invalid INTEGER, outside INTEGER)")
                db.beginTransaction()
            } catch (e: Exception) { db.close(); throw e }
        }
        fun sink(feed: Int): EpgSink {
            require(feed in plan.urls.indices)
            return object : EpgSink {
                override fun channel(value: EpgChannel) {
                    checkWriter()
                    check(!sealed && !closed)
                    db.execSQL("INSERT OR IGNORE INTO channels VALUES(?,?)", arrayOf<Any>(feed, value.id))
                    value.names.forEach { name -> db.execSQL("INSERT OR IGNORE INTO aliases VALUES(?,?,?)", arrayOf<Any>(epgNameKey(name), feed, value.id)) }
                }
                override fun programme(value: EpgProgramme) {
                    checkWriter()
                    check(!sealed && !closed)
                    check(++inserted <= 1_000_000)
                    // XMLTV allows programmes without a preceding channel definition.
                    db.execSQL("INSERT OR IGNORE INTO channels VALUES(?,?)", arrayOf<Any>(feed, value.channelId))
                    db.execSQL("INSERT OR IGNORE INTO programmes VALUES(?,?,?,?,?,?,?)", arrayOf<Any>(feed, value.channelId,
                        value.start, value.end ?: -1, value.title, value.description, value.category))
                }
            }
        }
        fun seal(updatedAt: Long, reports: List<EpgParseReport>) {
            checkWriter()
            check(!sealed && !closed && reports.size == plan.urls.size)
            if (reports.sumOf { it.programmes } == 0 && reports.sumOf { it.invalid } > 0)
                throw SourceFailure(FailureReason.GUIDE_ENTRIES_UNREADABLE)
            fun count(table: String) = db.rawQuery("SELECT COUNT(*) FROM $table", null).use { it.moveToFirst(); it.getInt(0) }
            db.execSQL("INSERT INTO snapshot VALUES(?,?,?,?,?,?,?)", arrayOf<Any>(updatedAt, window.from, window.until,
                count("channels"), count("programmes"), reports.sumOf { it.invalid }, reports.sumOf { it.outsideWindow }))
            db.setTransactionSuccessful(); db.endTransaction()
            db.close()
            sealed = true
        }
        override fun close() {
            if (closed) return
            checkWriter()
            try { if (db.isOpen) { if (db.inTransaction()) db.endTransaction(); db.close() } }
            finally {
                synchronized(lock) { deleteDatabaseFiles(path); activeFiles -= path.absolutePath; closed = true }
            }
        }
    }

    companion object {
        private val lock = Any()
        private val generations = mutableMapOf<String, Long>()
        private val activeFiles = mutableSetOf<String>()
        private fun deleteDatabaseFiles(path: File) {
            listOf(path, File(path.path + "-journal"), File(path.path + "-wal"), File(path.path + "-shm")).forEach { it.delete() }
        }
    }
}
