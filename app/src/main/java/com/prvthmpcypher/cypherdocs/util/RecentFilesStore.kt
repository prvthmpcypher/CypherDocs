package com.prvthmpcypher.cypherdocs.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class RecentFile(
    val uri: String,
    val name: String,
    val mimeType: String?,
    val parentTreeUri: String?,
    val timestamp: Long
)

/**
 * Simple on-device (no cloud, no network) list of recently opened files.
 */
object RecentFilesStore {

    private const val PREFS = "cypherdocs_prefs"
    private const val KEY_RECENTS = "recent_files_json"
    private const val MAX_ENTRIES = 25

    fun add(context: Context, entry: RecentFile) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = loadAll(context).toMutableList()
        existing.removeAll { it.uri == entry.uri }
        existing.add(0, entry)
        val trimmed = existing.take(MAX_ENTRIES)

        val arr = JSONArray()
        trimmed.forEach { r ->
            val obj = JSONObject()
            obj.put("uri", r.uri)
            obj.put("name", r.name)
            obj.put("mimeType", r.mimeType ?: "")
            obj.put("parentTreeUri", r.parentTreeUri ?: "")
            obj.put("timestamp", r.timestamp)
            arr.put(obj)
        }
        prefs.edit().putString(KEY_RECENTS, arr.toString()).apply()
    }

    fun loadAll(context: Context): List<RecentFile> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                RecentFile(
                    uri = obj.getString("uri"),
                    name = obj.getString("name"),
                    mimeType = obj.optString("mimeType").ifBlank { null },
                    parentTreeUri = obj.optString("parentTreeUri").ifBlank { null },
                    timestamp = obj.optLong("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_RECENTS).apply()
    }
}
