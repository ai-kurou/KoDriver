package kurou.kodriver.feature.otherthemedetail

import kurou.kodriver.domain.model.ThemeMode

internal data class OtherThemeDetailUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val pendingThemeMode: ThemeMode = ThemeMode.SYSTEM,
)
