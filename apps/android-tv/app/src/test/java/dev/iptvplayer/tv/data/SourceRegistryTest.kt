package dev.iptvplayer.tv.data

import org.junit.Assert.*
import org.junit.Test

class SourceRegistryTest {
    private val a = SourceConfig("a", "Ev", SourceType.XTREAM, "https://example.invalid/", "demo", "test-secret")
    private val b = SourceConfig("b", "Diğer", SourceType.M3U, "https://example.invalid/list.m3u")
    @Test fun credentialsAndFilterRoundTripOnlyThroughCodec() {
        val registry = SourceRegistry(listOf(a,b), b.id)
        assertEquals(registry, SourceRegistryCodec.decode(SourceRegistryCodec.encode(registry)))
        assertFalse(registry.toString().contains("test-secret"))
        assertFalse(registry.toString().contains("example.invalid"))
    }
    @Test fun editingKeepsSourceOrderAndOtherCredentials() {
        val changed = SourceRegistry(listOf(a,b),a.id).upsert(a.copy(name="Yeni", password="new-fixture"))
        assertEquals(listOf("a","b"), changed.sources.map { it.id })
        assertEquals(b, changed.sources.last())
        assertEquals("a", changed.filterId)
    }
    @Test fun deletingTheFilteredSourceFallsBackToAllRemainingSources() {
        assertEquals(SourceRegistry(listOf(b)), SourceRegistry(listOf(a,b),a.id).without(a.id))
        assertEquals(SourceRegistry(), SourceRegistry(listOf(a),a.id).without(a.id))
    }
    @Test fun duplicateIdsAndMissingFilterAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { SourceRegistry(listOf(a,a)).checked() }
        assertThrows(IllegalArgumentException::class.java) { SourceRegistry(listOf(a),"missing").checked() }
    }
    @Test fun unknownVersionOrInvalidSourceCannotBecomeAnEmptyRegistry() {
        assertThrows(Exception::class.java) { SourceRegistryCodec.decode("{\"version\":99,\"sources\":[]}".toByteArray()) }
        val invalid = SourceRegistryCodec.encode(SourceRegistry(listOf(b.copy(address="file:///private"))))
        assertThrows(SourceFailure::class.java) { SourceRegistryCodec.decode(invalid) }
    }
}
