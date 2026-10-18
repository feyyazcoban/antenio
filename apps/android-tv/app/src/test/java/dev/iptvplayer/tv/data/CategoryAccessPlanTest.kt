package dev.iptvplayer.tv.data

import dev.iptvplayer.tv.data.CategoryAccess.*
import org.junit.Assert.*
import org.junit.Test

class CategoryAccessPlanTest {
    private val noPin = ParentalUi(status = ParentalStatus.OFF)
    private val pinAsked = ParentalUi(status = ParentalStatus.ON)
    private val pinVerified = ParentalUi(status = ParentalStatus.ON, adminUnlocked = true)
    private val loading = ParentalUi(status = ParentalStatus.LOADING)
    private val unreadable = ParentalUi(status = ParentalStatus.UNAVAILABLE)
    private fun plan(current: CategoryAccess, target: CategoryAccess, parental: ParentalUi) = CategoryAccessPlan.of(current, target, parental)

    @Test fun theChoiceTheRowAlreadyHasDoesNothing() {
        for (access in CategoryAccess.entries) for (parental in listOf(noPin, pinAsked, pinVerified, unreadable))
            assertEquals(CategoryAccessPlan.None, plan(access, access, parental))
    }

    @Test fun kilitleWithoutAPinMakesOneFirstFromAShownOrAHiddenRow() {
        assertEquals(CategoryAccessPlan.CreatePin, plan(SHOWN, LOCKED, noPin))
        assertEquals(CategoryAccessPlan.CreatePin, plan(HIDDEN, LOCKED, noPin))
        // Once the PIN is saved its pad grants Ayarlar, and the follow-up locks the row (a hidden one comes back after).
        assertEquals(CategoryAccessPlan.Change(lock = true, unhide = false), plan(SHOWN, LOCKED, pinVerified))
        assertEquals(CategoryAccessPlan.Change(lock = true, unhide = true), plan(HIDDEN, LOCKED, pinVerified))
    }

    @Test fun gizleTightensAndAsksForNothingAndKeepsALock() {
        for (parental in listOf(noPin, pinAsked, pinVerified, loading, unreadable)) {
            assertEquals(CategoryAccessPlan.Hide, plan(SHOWN, HIDDEN, parental))
            // The lock stays under the hide: only Göster removes its rules.
            assertEquals(CategoryAccessPlan.Hide, plan(LOCKED, HIDDEN, parental))
        }
    }

    @Test fun kilitleOnAShownRowTightensWithoutTheVerifiedPin() {
        assertEquals(CategoryAccessPlan.Change(lock = true, unhide = false), plan(SHOWN, LOCKED, pinAsked))
        // A PIN record not read yet, or unreadable, cannot take a lock.
        assertEquals(CategoryAccessPlan.None, plan(SHOWN, LOCKED, loading))
        assertEquals(CategoryAccessPlan.None, plan(SHOWN, LOCKED, unreadable))
    }

    @Test fun showingOrBringingBackARowLoosensAndNeedsThePinVerifiedInThisVisit() {
        for (parental in listOf(pinAsked, loading, unreadable)) {
            assertEquals(CategoryAccessPlan.None, plan(LOCKED, SHOWN, parental))
            assertEquals(CategoryAccessPlan.None, plan(HIDDEN, SHOWN, parental))
            assertEquals(CategoryAccessPlan.None, plan(HIDDEN, LOCKED, parental))
        }
        assertEquals(CategoryAccessPlan.Change(lock = false, unhide = false), plan(LOCKED, SHOWN, pinVerified))
        // Göster on a hidden row removes a lock it kept under the hide, then shows it.
        assertEquals(CategoryAccessPlan.Change(lock = false, unhide = true), plan(HIDDEN, SHOWN, pinVerified))
        // Without a PIN nothing guards it.
        assertEquals(CategoryAccessPlan.Change(lock = false, unhide = true), plan(HIDDEN, SHOWN, noPin))
    }
}
