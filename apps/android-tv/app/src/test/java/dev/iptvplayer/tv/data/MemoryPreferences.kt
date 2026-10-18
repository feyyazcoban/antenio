package dev.iptvplayer.tv.data

import android.content.SharedPreferences

/** SharedPreferences in memory, so DeviceStore's preference calls run without a device; null in a change removes. */
internal class MemoryPreferences : SharedPreferences {
    val values = HashMap<String, Any>()
    override fun getAll(): Map<String, *> = HashMap(values)
    override fun getString(key: String, defValue: String?) = values[key] as String? ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?) = values[key] as Set<String>? ?: defValues
    override fun getInt(key: String, defValue: Int) = values[key] as Int? ?: defValue
    override fun getLong(key: String, defValue: Long) = values[key] as Long? ?: defValue
    override fun getFloat(key: String, defValue: Float) = values[key] as Float? ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = values[key] as Boolean? ?: defValue
    override fun contains(key: String) = key in values
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val changes = LinkedHashMap<String, Any?>()
        private var clearFirst = false
        private fun change(key: String, value: Any?): SharedPreferences.Editor { changes[key] = value; return this }
        override fun putString(key: String, value: String?) = change(key, value)
        override fun putStringSet(key: String, values: Set<String>?) = change(key, values?.toSet())
        override fun putInt(key: String, value: Int) = change(key, value)
        override fun putLong(key: String, value: Long) = change(key, value)
        override fun putFloat(key: String, value: Float) = change(key, value)
        override fun putBoolean(key: String, value: Boolean) = change(key, value)
        override fun remove(key: String) = change(key, null)
        override fun clear(): SharedPreferences.Editor { clearFirst = true; return this }
        override fun commit(): Boolean {
            if (clearFirst) values.clear()
            changes.forEach { (key, value) -> if (value == null) values.remove(key) else values[key] = value }
            return true
        }
        override fun apply() { commit() }
    }
}
