package dev.iptvplayer.tv.data

/** A point event occupies one millisecond of navigation space, never an invented programme duration. */
data class GuideSlot(val from: Long, val until: Long, val programmes: List<EpgProgramme>) {
    val programme: EpgProgramme? get() = programmes.singleOrNull()
    val point: Boolean get() = programme?.end == null && programmes.size == 1
    fun contains(time: Long) = time >= from && time < until
}

/** Sweep line partitions time into programmes, gaps and explicit overlaps in O(n log n). */
fun guideSlots(programmes: List<EpgProgramme>, window: GuideWindow): List<GuideSlot> {
    data class Event(val time: Long, val index: Int, val starts: Boolean)
    val events = buildList {
        programmes.forEachIndexed { index, programme ->
            val start = maxOf(window.from, programme.start)
            val stop = minOf(window.until, programme.end ?: (programme.start + 1))
            if (start < stop) { add(Event(start, index, true)); add(Event(stop, index, false)) }
        }
    }.sortedWith(compareBy({ it.time }, { it.starts }))
    val active = linkedSetOf<Int>()
    val result = mutableListOf<GuideSlot>()
    var time = window.from
    var index = 0
    while (index < events.size) {
        val at = events[index].time
        if (at > time) result += GuideSlot(time, at, active.map(programmes::get))
        while (index < events.size && events[index].time == at) {
            val event = events[index++]
            if (event.starts) active.add(event.index) else active.remove(event.index)
        }
        time = at
    }
    if (time < window.until) result += GuideSlot(time, window.until, active.map(programmes::get))
    return result
}

fun guideHorizontalTarget(slots: List<GuideSlot>, anchor: Long, direction: Int): Long {
    val index = slots.indexOfFirst { it.contains(anchor) }
    if (index < 0) return anchor
    return if (direction > 0) slots[index].until else if (index > 0) slots[index - 1].from else slots.first().from - 1
}
