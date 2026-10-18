package dev.iptvplayer.tv.data

/**
 * What PINs typed for locked titles opened (pin.md §2, §6): the scopes of the rules that locked each one, so a PIN for
 * a channel of a locked category opens that category's other channels as well, never the whole source. Memory only.
 * It ends when the app leaves the screen, the PIN changes, a lock or a hidden category is loosened, or with "Açılan
 * kilitleri kapat"; a lock added keeps it, as a tightening asks for nothing.
 */
internal class ParentalSession {
    var grants: Set<AccessScope> = emptySet()
        private set

    /** Every scope that locks a title is open; a title one more rule locks asks again. */
    fun covers(scopes: Set<AccessScope>): Boolean = grants.containsAll(scopes)

    fun grant(scopes: Set<AccessScope>) { grants = grants + scopes }

    fun clear() { grants = emptySet() }

    /** The rules went from [old] to [new]: kept only when every restriction of [old] is still there unchanged. */
    fun policyChanged(old: List<AccessRule>, new: List<AccessRule>) {
        if (!restrictions(new).containsAll(restrictions(old))) clear()
    }

    private fun restrictions(rules: List<AccessRule>) = rules.filterTo(HashSet()) { it.hidden || it.pinRequired }
}
