package com.pga.magiccollection.ui.designsystem

/**
 * Re-exports the canonical spacing tokens from the `ui.theme` package so that older call
 * sites importing from `ui.designsystem.*` keep resolving. The single source of truth is
 * [com.pga.magiccollection.ui.theme.LocalAppSpacing].
 */
typealias AppSpacing = com.pga.magiccollection.ui.theme.AppSpacing

val LocalAppSpacing = com.pga.magiccollection.ui.theme.LocalAppSpacing
