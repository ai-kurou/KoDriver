package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/** rememberAccessLocalNetworkPermissionRequester のこのプラットフォーム向け実装。権限が不要なため何も行わない。 */
@Composable
actual fun rememberAccessLocalNetworkPermissionRequester(onResult: () -> Unit): () -> Unit = {}
