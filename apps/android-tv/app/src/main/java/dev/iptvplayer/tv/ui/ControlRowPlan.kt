package dev.iptvplayer.tv.ui

/** Measured pixels, in the explicit D072 label-drop order. No focus-dependent measurements or horizontal scroll. */
internal object ControlRowPlan {
    data class Chip(val key: String, val labelled: Int, val icon: Int, val priority: Int)
    data class Plan(val labels: Set<String>, val hints: Int)
    fun fit(width: Int, cluster: Int, chips: List<Chip>, gap: Int, separation: Int,
        hintWidths: List<Int>, large: Boolean): Plan {
        val labels = chips.filter { it.labelled > it.icon && !large }.mapTo(mutableSetOf()) { it.key }
        fun chipWidth() = chips.sumOf { if (it.key in labels) it.labelled else it.icon } + gap * (chips.size - 1).coerceAtLeast(0)
        for (chip in chips.sortedBy { it.priority }) {
            if (cluster + separation + chipWidth() <= width) break
            labels.remove(chip.key)
        }
        val spare = width - cluster - chipWidth() - separation
        var used = 0
        var hints = 0
        if (!large) for (part in hintWidths) {
            if (used + part > spare) break
            used += part; hints++
        }
        return Plan(labels, hints)
    }
}
