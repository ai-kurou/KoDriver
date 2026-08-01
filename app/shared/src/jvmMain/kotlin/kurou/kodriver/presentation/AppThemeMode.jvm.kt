package kurou.kodriver.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kurou.kodriver.feature.otherthemedetail.rememberOtherThemeDarkTheme

@Composable
internal actual fun rememberAppDarkTheme(): Boolean = rememberOtherThemeDarkTheme(systemDarkTheme = isSystemInDarkTheme())
