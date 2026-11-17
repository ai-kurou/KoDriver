package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable

/**
 * rememberOpenAccessLocalNetworkPermissionSettings のこのプラットフォーム向け実装。
 * Android専用の権限のため何も行わない。
 */
@Composable
public actual fun rememberOpenAccessLocalNetworkPermissionSettings(): () -> Unit = {}
