package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/**
 * ローカルネットワークの権限を要求する関数を取得する。許可・拒否の結果が出たあとに [onResult] を呼ぶ。
 * 恒久的に拒否されていて要求のダイアログを出せない場合は、アプリの設定画面を開く。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
@Composable
expect fun rememberAccessLocalNetworkPermissionRequester(onResult: () -> Unit): () -> Unit
