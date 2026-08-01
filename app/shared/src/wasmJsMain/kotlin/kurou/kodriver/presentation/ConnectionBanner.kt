package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/**
 * rememberConnectionBannerUiState のこのプラットフォーム向け実装。
 */
@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState = ConnectionBannerUiState()
