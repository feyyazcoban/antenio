package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class ParentalSessionTest {
    private val sports = AccessScope.Category("a", MediaKind.LIVE, AccessCategory.ProviderId("10"))
    private val film = AccessScope.Content(ContentRef("a", "movie:3"))
    private val list = AccessScope.Source("b")
    private fun lock(scope: AccessScope) = AccessRule(scope, pinRequired = true)

    @Test fun aPinOpensOnlyTheScopesThatLockedItsTitle() {
        val session = ParentalSession()
        assertTrue(session.covers(emptySet()))
        assertFalse(session.covers(setOf(sports)))
        session.grant(setOf(sports))
        // Another channel of the same locked category opens; a title one more rule locks asks again.
        assertTrue(session.covers(setOf(sports)))
        assertFalse(session.covers(setOf(sports, list)))
        assertFalse(session.covers(setOf(film)))
        session.grant(setOf(film))
        assertEquals(setOf(sports, film), session.grants)
        session.clear()
        assertTrue(session.grants.isEmpty())
        assertFalse(session.covers(setOf(sports)))
    }

    @Test fun aLockAddedKeepsTheSessionAndAnyLooseningEndsIt() {
        val session = ParentalSession()
        val before = listOf(lock(sports), lock(film))
        session.grant(setOf(sports, film))
        session.policyChanged(before, before + lock(list))
        assertEquals(setOf(sports, film), session.grants)
        // The same rules in another order change nothing.
        session.policyChanged(before + lock(list), listOf(lock(list), lock(film), lock(sports)))
        assertEquals(setOf(sports, film), session.grants)
        // A rule that restricts nothing does not count.
        session.policyChanged(before, before + AccessRule(list))
        assertEquals(setOf(sports, film), session.grants)
        session.policyChanged(before, listOf(lock(sports)))
        assertTrue(session.grants.isEmpty())

        // A lock turned into something else counts as gone too: the session would rather ask again.
        session.grant(setOf(sports))
        session.policyChanged(listOf(lock(sports)), listOf(AccessRule(sports, hidden = true)))
        assertTrue(session.grants.isEmpty())
        // Every lock removed with the PIN.
        session.grant(setOf(sports))
        session.policyChanged(listOf(lock(sports)), emptyList())
        assertTrue(session.grants.isEmpty())
    }
}
