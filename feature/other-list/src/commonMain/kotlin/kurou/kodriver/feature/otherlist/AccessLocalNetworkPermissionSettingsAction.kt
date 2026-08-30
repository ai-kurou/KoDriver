package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable

/**
 * ACCESS_LOCAL_NETWORK 権限のアプリ設定画面を開くアクションを取得する。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
@Composable
expect fun rememberOpenAccessLocalNetworkPermissionSettings(): () -> Unit
