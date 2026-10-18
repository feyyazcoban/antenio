package dev.iptvplayer.tv.data

import org.json.JSONObject
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
        for (version in listOf(0, 3, 99)) assertThrows(Exception::class.java) {
            SourceRegistryCodec.decode("{\"version\":$version,\"sources\":[]}".toByteArray())
        }
        val invalid = SourceRegistryCodec.encode(SourceRegistry(listOf(b.copy(address="file:///private"))))
        assertEquals(FailureReason.ADDRESS_INVALID, assertThrows(SourceFailure::class.java) { SourceRegistryCodec.decode(invalid) }.reason)
    }

    /** A registry as 0.14.43 and every build before S07 wrote it. */
    private val versionOne = """{"version":1,"filter":"b","sources":[
        {"id":"a","name":"Ev","type":"XTREAM","address":"https://example.invalid/","username":"demo","password":"test-secret","epgUrl":""},
        {"id":"b","name":"Diğer","type":"M3U","address":"https://example.invalid/list.m3u","username":"","password":"","epgUrl":""}]}"""
    private fun JSONObject.fields() = keys().asSequence().associateWith { get(it) }

    @Test fun aVersionOneRecordOpensAndIsSavedAsVersionTwoWithTheSameFields() {
        val registry = SourceRegistryCodec.decode(versionOne.toByteArray())
        assertEquals(SourceRegistry(listOf(a,b), b.id), registry)
        val saved = JSONObject(SourceRegistryCodec.encode(registry).toString(Charsets.UTF_8))
        assertEquals(2, saved.getInt("version"))
        val old = JSONObject(versionOne)
        assertEquals(old.getString("filter"), saved.getString("filter"))
        for (i in 0 until 2) assertEquals(old.getJSONArray("sources").getJSONObject(i).fields(), saved.getJSONArray("sources").getJSONObject(i).fields())
        assertEquals(registry, SourceRegistryCodec.decode(saved.toString().toByteArray()))
    }

    @Test fun keysThisBuildDoesNotKnowAreSkipped() {
        val newer = """{"version":2,"later":{"sync":true},"sources":[{"id":"a","name":"Ev","type":"XTREAM",
            "address":"https://example.invalid/","username":"demo","password":"test-secret","epgUrl":"",
            "limits":{"streams":1},"connection":{"userAgent":"Fixture/1","headers":[]},
            "guides":[{"id":"g","url":"https://example.invalid/guide.xml"}],"note":"later"}]}"""
        assertEquals(SourceRegistry(listOf(a)), SourceRegistryCodec.decode(newer.toByteArray()))
    }

    @Test fun printingARegistryOrASourceShowsNoAccountOrAddress() {
        val guided = b.copy(id = "c", epgUrl = "https://guide.example.invalid/tv.xml")
        val registry = SourceRegistryCodec.decode(SourceRegistryCodec.encode(SourceRegistry(listOf(a, guided), a.id)))
        for (text in listOf(registry.toString()) + registry.sources.map { it.toString() }) {
            for (secret in listOf("demo", "test-secret", "example.invalid", "list.m3u", "tv.xml")) assertFalse(text, text.contains(secret))
        }
    }
}
