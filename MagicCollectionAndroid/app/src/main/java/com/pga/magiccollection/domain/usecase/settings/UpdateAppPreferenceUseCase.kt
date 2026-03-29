package com.pga.magiccollection.domain.usecase.settings

import com.pga.magiccollection.data.local.security.PreferenceManager
import javax.inject.Inject

class UpdateAppPreferenceUseCase @Inject constructor(
    private val preferenceManager: PreferenceManager
) {
    suspend fun setDarkTheme(enabled: Boolean) = preferenceManager.setDarkTheme(enabled)
    suspend fun setGridSize(size: Int) = preferenceManager.setGridSize(size)
    suspend fun setStartScreen(route: String) = preferenceManager.setStartScreen(route)
    suspend fun setSearchLanguage(lang: String) = preferenceManager.setSearchLanguage(lang)
    suspend fun setAppLanguage(lang: String) = preferenceManager.setAppLanguage(lang)
}
