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
    val appLanguage: String
)

class GetAppPreferencesUseCase @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    operator fun invoke(): Flow<AppPreferences> {
        return combine(
            preferenceManager.darkTheme,
            preferenceManager.gridSize,
            preferenceManager.startScreen,
            preferenceManager.searchLanguage,
            preferenceManager.appLanguage
        ) { dark, grid, start, search, app ->
            AppPreferences(dark, grid, start, search, app)
        }
    }
}
