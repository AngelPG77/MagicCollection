package com.pga.magiccollection.domain.usecase.settings

import com.pga.magiccollection.data.local.security.PreferenceManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class AppPreferences(
    val darkTheme: Boolean,
    val gridSize: Int,
    val startScreen: String,
    val searchLanguage: String,
    val appLanguage: String,
    val themeColor: String,
    val downloadedLanguages: Set<String>,
    val lastIndexUpdate: String?
)

class GetAppPreferencesUseCase @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(): Flow<AppPreferences> {
        return combine(
            preferenceManager.darkTheme,
            preferenceManager.gridSize,
            preferenceManager.startScreen,
            preferenceManager.searchLanguage,
            preferenceManager.appLanguage,
            preferenceManager.themeColor,
            preferenceManager.downloadedLanguages,
            preferenceManager.lastIndexUpdate
        ) { values: Array<Any?> ->
            AppPreferences(
                darkTheme = values[0] as Boolean,
                gridSize = values[1] as Int,
                startScreen = values[2] as String,
                searchLanguage = values[3] as String,
                appLanguage = values[4] as String,
                themeColor = values[5] as String,
                downloadedLanguages = values[6] as Set<String>,
                lastIndexUpdate = values[7] as String?
            )
        }
    }
}
