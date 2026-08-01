package kurou.kodriver.feature.otherthemedetail

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.domain.model.ThemeMode
import org.koin.compose.viewmodel.koinViewModel

/**
 * rememberOtherThemeDarkTheme を Compose の状態として生成・保持する。
 */
@Composable
fun rememberOtherThemeDarkTheme(
    systemDarkTheme: Boolean,
    viewModel: OtherThemeDetailViewModel = koinViewModel(),
): Boolean {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    return resolveDarkTheme(
        themeMode = uiState.value.selectedThemeMode,
        systemDarkTheme = systemDarkTheme,
    )
}

internal fun resolveDarkTheme(
    themeMode: ThemeMode,
    systemDarkTheme: Boolean,
): Boolean =
    when (themeMode) {
        ThemeMode.SYSTEM -> systemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
