package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class AccessPolicyTest {
    private val source = SourceConfig("a", "Ev", SourceType.XTREAM, "http://127.0.0.1/")
    private val movie = MediaEntry("movie:1", "1", MediaKind.MOVIE, "Film", "Aile", categoryId = "10").inSource("a")
    private val show = MediaEntry("series:2", "2", MediaKind.SERIES, "Dizi", "Aile", categoryId = "10").inSource("a")
    private val episode = MediaEntry("episode:3", "3", MediaKind.EPISODE, "Bölüm", parentLocalId = "series:2").inSource("a")
    private fun rule(scope: AccessScope, hidden: Boolean = false) = AccessRule(scope, hidden, pinRequired = !hidden)
    private fun content(entry: MediaEntry) = AccessScope.Content(ContentRef(entry.sourceId, entry.localId!!))
    private fun category(kind: MediaKind = MediaKind.MOVIE) = AccessScope.Category("a", kind, AccessCategory.ProviderId("10"))

    @Test fun noPolicyAllowsOwnedContentButNotMissingSourceAdultOrForgedIdentity() {
        val policy = AccessPolicy(emptyList())
        assertEquals(AccessDecision.ALLOW, policy.decide(source, movie))
        for (bad in listOf(movie.copy(adult = true), movie.copy(id = "forged"), movie.copy(sourceId = "b"), movie.withoutSource()))
            assertEquals(AccessDecision.DENIED, policy.decide(source, bad))
        assertEquals(AccessDecision.DENIED, policy.decide(null, movie))
        assertEquals(AccessDecision.DENIED, policy.decide(source.copy(id = ""), movie))
    }
    @Test fun sourceLockCannotBeOverriddenByAnEmptyContentRule() {
        val policy = AccessPolicy(listOf(rule(AccessScope.Source("a")), AccessRule(content(movie))))
        assertEquals(AccessDecision.REQUIRE_PIN, policy.decide(source, movie))
        assertTrue(policy.decide(source, movie).visible)
        assertFalse(policy.decide(source, movie).canOpen)
    }
    @Test fun hidingAndProtectionAreIndependentAndHidingWins() {
        assertEquals(AccessDecision.HIDDEN, AccessPolicy(listOf(rule(content(movie), true), rule(AccessScope.Source("a")))).decide(source, movie))
        assertEquals(AccessDecision.HIDDEN, AccessPolicy(listOf(rule(AccessScope.Source("a"), true))).decide(source, movie))
        assertFalse(AccessDecision.HIDDEN.visible)
    }
    @Test fun categoryUsesIdNotNameAndSeparatesKindsAndSources() {
        val policy = AccessPolicy(listOf(rule(category())))
        assertEquals(AccessDecision.REQUIRE_PIN, policy.decide(source, movie.copy(category = "Yeni ad")))
        assertEquals(AccessDecision.ALLOW, policy.decide(source, movie.copy(categoryId = "20")))
        assertEquals(AccessDecision.ALLOW, policy.decide(source, show))
        assertEquals(AccessDecision.ALLOW, policy.decide(source.copy(id = "b"), movie.withoutSource().inSource("b")))
    }
    @Test fun missingOldCategoryIdentityDoesNotBypassCategoryRule() {
        assertEquals(AccessDecision.UNRESOLVED, AccessPolicy(listOf(rule(category()))).decide(source, movie.copy(categoryId = "")))
    }
    @Test fun m3uCategoryLabelHasSeparateNamespaceAndDoesNotNormalizeNames() {
        val m3u = source.copy(type = SourceType.M3U)
        val policy = AccessPolicy(listOf(rule(AccessScope.Category("a", MediaKind.MOVIE, AccessCategory.M3uName("Aile")))))
        assertEquals(AccessDecision.REQUIRE_PIN, policy.decide(m3u, movie))
        assertEquals(AccessDecision.ALLOW, policy.decide(m3u, movie.copy(category = "aile")))
        assertEquals(AccessDecision.UNRESOLVED, policy.decide(source, movie))
        assertEquals(AccessDecision.UNRESOLVED, AccessPolicy(listOf(rule(category()))).decide(m3u, movie))
    }
    @Test fun episodeInheritsSeriesContentAndCategoryRestrictions() {
        val context = AccessPolicy.SeriesContext(show, listOf(episode))
        assertEquals(AccessDecision.REQUIRE_PIN, AccessPolicy(listOf(rule(content(show)))).decide(source, episode, context))
        assertEquals(AccessDecision.HIDDEN, AccessPolicy(listOf(rule(category(MediaKind.SERIES), true))).decide(source, episode, context))
        assertEquals(AccessDecision.HIDDEN, AccessPolicy(listOf(rule(content(episode), true))).decide(source, episode))
    }
    @Test fun wrongOrMissingEpisodeParentCannotBypassRestrictions() {
        val policy = AccessPolicy(listOf(rule(content(show))))
        for (context in listOf(null, AccessPolicy.SeriesContext(show, emptyList()),
            AccessPolicy.SeriesContext(movie, listOf(episode)),
            AccessPolicy.SeriesContext(show.withoutSource().inSource("b"), listOf(episode)),
            AccessPolicy.SeriesContext(show.copy(id = ContentRef("a", "series:9").key, localId = "series:9"), listOf(episode))))
            assertEquals(AccessDecision.UNRESOLVED, policy.decide(source, episode, context))
    }
    @Test fun legacyEpisodeCanResolveThroughVerifiedMembership() {
        val old = episode.copy(parentLocalId = null)
        val policy = AccessPolicy(listOf(rule(content(show))))
        assertEquals(AccessDecision.REQUIRE_PIN, policy.decide(source, old, AccessPolicy.SeriesContext(show, listOf(old))))
        assertEquals(AccessDecision.ALLOW, AccessPolicy(listOf(rule(AccessScope.Source("b")))).decide(source, old))
    }
    @Test fun adultParentIsDeniedEvenWithoutRules() {
        assertEquals(AccessDecision.DENIED, AccessPolicy(emptyList()).decide(source, episode,
            AccessPolicy.SeriesContext(show.copy(adult = true), listOf(episode))))
    }
    @Test fun policyCopiesRuleListAndRejectsInvalidScope() {
        val rules = mutableListOf(rule(AccessScope.Source("a")))
        val policy = AccessPolicy(rules, 7); rules.clear()
        assertEquals(7L, policy.revision)
        assertEquals(AccessDecision.REQUIRE_PIN, policy.decide(source, movie))
        assertThrows(IllegalArgumentException::class.java) { AccessPolicy(emptyList(), -1) }
        assertThrows(IllegalArgumentException::class.java) { AccessScope.Category("a", MediaKind.EPISODE, AccessCategory.ProviderId("1")) }
    }

    @Test fun ruleOrderAndUnrelatedRulesCannotWeakenARestriction() {
        val rules = listOf(rule(AccessScope.Source("a")), rule(category(), true), AccessRule(content(movie)),
            rule(AccessScope.Source("b"), true))
        for (offset in rules.indices) {
            val rotated = rules.drop(offset) + rules.take(offset)
            for (ordering in listOf(rotated, rotated.reversed()))
                assertEquals(AccessDecision.HIDDEN, AccessPolicy(ordering).decide(source, movie))
        }
        for (decision in AccessDecision.entries) {
            if (decision != AccessDecision.ALLOW) assertFalse(decision.canOpen)
            if (decision == AccessDecision.DENIED || decision == AccessDecision.UNRESOLVED) assertFalse(decision.visible)
        }
    }
}
