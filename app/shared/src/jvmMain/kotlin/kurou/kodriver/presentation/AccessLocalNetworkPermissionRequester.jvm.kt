package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/** rememberAccessLocalNetworkPermissionRequester のこのプラットフォーム向け実装。権限が不要なため何も行わない。 */
@Composable
internal actual fun rememberAccessLocalNetworkPermissionRequester(
    onResult: () -> Unit,
): (openSettingsWhenPermanentlyDenied: Boolean) -> Unit = {}
