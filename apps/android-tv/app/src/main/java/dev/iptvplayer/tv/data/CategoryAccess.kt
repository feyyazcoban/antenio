package dev.iptvplayer.tv.data

/** A category row in Kategorileri yönet (pin.md §3.4): shown, locked (listed, its titles open with the PIN) or hidden. */
enum class CategoryAccess { SHOWN, LOCKED, HIDDEN }

/**
 * What Göster, Kilitle or Gizle on a category row does (pin.md §0, §3.4), decided before anything is written. Hiding and
 * locking tighten and ask for nothing. Showing a hidden or locked row loosens, as does a hidden row coming back locked:
 * with a PIN set, only once it was verified in this Settings visit. Locking needs a PIN that can be read; on a TV without
 * one, a first PIN is made first. Pure, so every path is tested on the JVM; TvViewModel.setCategoryAccess carries it out.
 */
internal sealed interface CategoryAccessPlan {
    /** Nothing changes: the row already stands so, the change loosens without the PIN, or the PIN record is not read. */
    data object None : CategoryAccessPlan
    /** Kilitle on a TV without a PIN: the pad makes a first one, and the row locks once it is saved. */
    data object CreatePin : CategoryAccessPlan
    /** Gizle: the row hides, and a lock it had stays under the hide for Göster to remove. */
    data object Hide : CategoryAccessPlan
    /**
     * The row's lock rules are added ([lock]) or removed; only once that is saved does a hidden row come back
     * ([unhide]), so a row on its way to Kilitli never shows unlocked.
     */
    data class Change(val lock: Boolean, val unhide: Boolean) : CategoryAccessPlan

    companion object {
        /** What choosing [target] for a row that stands at [current] does, with the PIN as [parental] has it. */
        fun of(current: CategoryAccess, target: CategoryAccess, parental: ParentalUi): CategoryAccessPlan = when {
            target == current -> None
            target == CategoryAccess.LOCKED && parental.status == ParentalStatus.OFF -> CreatePin
            target == CategoryAccess.HIDDEN -> Hide
            // From here the row is shown, or comes back from hiding locked.
            (current == CategoryAccess.HIDDEN || target == CategoryAccess.SHOWN) && !parental.mayLoosen -> None
            target == CategoryAccess.LOCKED && parental.status != ParentalStatus.ON -> None
            else -> Change(lock = target == CategoryAccess.LOCKED, unhide = current == CategoryAccess.HIDDEN)
        }
    }
}
