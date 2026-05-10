package com.pga.magiccollection.data.local.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val GRID_SIZE = intPreferencesKey("grid_size")
        private val START_SCREEN = stringPreferencesKey("start_screen")
        private val SEARCH_LANGUAGE = stringPreferencesKey("search_language")
        private val APP_LANGUAGE = stringPreferencesKey("app_language")
        private val THEME_COLOR = stringPreferencesKey("theme_color")
        private val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val DOWNLOADED_LANGS = stringPreferencesKey("downloaded_languages")
        private val LAST_INDEX_UPDATE = stringPreferencesKey("last_index_update")
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[DARK_THEME] ?: false }
    val gridSize: Flow<Int> = context.dataStore.data.map { it[GRID_SIZE] ?: 3 }
    val startScreen: Flow<String> = context.dataStore.data.map { it[START_SCREEN] ?: "home" }
    val searchLanguage: Flow<String> = context.dataStore.data.map { it[SEARCH_LANGUAGE] ?: "en" }
    val appLanguage: Flow<String> = context.dataStore.data.map { it[APP_LANGUAGE] ?: "system" }
    val themeColor: Flow<String> = context.dataStore.data.map { it[THEME_COLOR] ?: "Azorius" }
    val dynamicColor: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLOR] ?: true }
    val lastIndexUpdate: Flow<String?> = context.dataStore.data.map { it[LAST_INDEX_UPDATE] }
    val downloadedLanguages: Flow<Set<String>> = context.dataStore.data.map {
        val normalized = it[DOWNLOADED_LANGS]
            ?.split(",")
            ?.asSequence()
            ?.map { s -> s.trim().lowercase() }
            ?.filter { s -> s.isNotBlank() }
            ?.toMutableSet()
            ?: mutableSetOf()
        normalized.add("en")
        normalized.toSet()
    }

    suspend fun setLastIndexUpdate(timestamp: String) {
        context.dataStore.edit { it[LAST_INDEX_UPDATE] = timestamp }
    }

    suspend fun setThemeColor(color: String) {
        context.dataStore.edit { it[THEME_COLOR] = color }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLOR] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun setGridSize(size: Int) {
        context.dataStore.edit { it[GRID_SIZE] = size.coerceIn(1, 6) }
    }

    suspend fun setStartScreen(route: String) {
        context.dataStore.edit { it[START_SCREEN] = route }
    }

    suspend fun setSearchLanguage(lang: String) {
        context.dataStore.edit { it[SEARCH_LANGUAGE] = lang.trim().lowercase().ifBlank { "en" } }
    }

    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { it[APP_LANGUAGE] = lang }
    }

    suspend fun addDownloadedLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[DOWNLOADED_LANGS]
                ?.split(",")
                ?.asSequence()
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotBlank() }
                ?.toMutableSet()
                ?: mutableSetOf()
            current.add("en")
            current.add(lang.trim().lowercase())
            prefs[DOWNLOADED_LANGS] = current.filter { it.isNotBlank() }.sorted().joinToString(",")
        }
    }
}
