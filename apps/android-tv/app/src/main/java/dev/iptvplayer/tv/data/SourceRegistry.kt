package dev.iptvplayer.tv.data

import org.json.JSONArray
import org.json.JSONObject

/** Credentials are encoded only inside DeviceStore's authenticated encrypted envelope. */
data class SourceRegistry(val sources: List<SourceConfig> = emptyList(), val filterId: String? = null) {
    fun checked(): SourceRegistry {
        require(sources.all { it.id.isNotBlank() })
        require(sources.map { it.id }.distinct().size == sources.size)
        require(filterId == null || sources.any { it.id == filterId })
        return this
    }

    fun upsert(source: SourceConfig): SourceRegistry {
        val index = sources.indexOfFirst { it.id == source.id }
        val next = sources.toMutableList().apply { if (index < 0) add(source) else set(index, source) }
        return copy(sources = next).checked()
    }

    fun without(id: String): SourceRegistry = copy(sources = sources.filterNot { it.id == id },
        filterId = filterId.takeUnless { it == id }).checked()

    override fun toString() = "SourceRegistry(count=${sources.size}, filterId=$filterId, credentials=REDACTED)"
}

/**
 * The registry as JSON (gap plan T5). Version 2 keeps every field of version 1 and makes room for optional objects per
 * source that later steps add without a new version: `limits` (S17), `connection` (S18) and `guides` (S19, which keeps
 * writing `epgUrl` beside it). Reading accepts versions 1 and 2 and skips keys it does not know, so a build opens a
 * record a newer one wrote; when that older build saves, the fields it does not know are gone. Builds before S07 accept
 * only version 1: put back on a TV over this one, they show the recovery screen and keep the record until the viewer
 * resets it. A version 1 record is rewritten as version 2 on the next save.
 */
internal object SourceRegistryCodec {
    const val VERSION = 2

    fun encode(registry: SourceRegistry): ByteArray {
        registry.checked()
        return JSONObject().put("version", VERSION).put("filter", registry.filterId).put("sources", JSONArray().apply {
            registry.sources.forEach { s -> put(JSONObject().put("id", s.id).put("name", s.name)
                .put("type", s.type.name).put("address", s.address).put("username", s.username).put("password", s.password).put("epgUrl", s.epgUrl)) }
        }).toString().toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): SourceRegistry {
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        require(json.getInt("version") in 1..VERSION)
        val list = json.getJSONArray("sources")
        val sources = (0 until list.length()).map { index ->
            val s = list.getJSONObject(index)
            SourceConfig(s.getString("id"), s.getString("name"), SourceType.valueOf(s.getString("type")),
                s.getString("address"), s.optString("username"), s.optString("password"), s.optString("epgUrl", "")).validated()
        }
        return SourceRegistry(sources, json.optString("filter").takeUnless { it.isBlank() || it == "null" }).checked()
    }
}
