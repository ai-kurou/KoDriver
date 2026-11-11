package kurou.kodriver.presentation

import androidx.compose.runtime.Composable

/**
 * ローカルネットワークの権限を要求する関数を取得する。許可・拒否の結果が出たあとに [onResult] を呼ぶ。
 * 返す関数の引数 `openSettingsWhenPermanentlyDenied` は、恒久的に拒否されていて要求のダイアログを出せない場合に、
 * アプリの設定画面を開くか（true: バナーのタップ）、何もしないか（false: 画面を開いたときの自動要求）を表す。
 * 恒久拒否の判定状態を画面を開いたときの要求とバナーのタップで共有するため、要求関数は画面ごとに1つだけ作る。
 * このプラットフォーム向けの実装を要求する expect 宣言。
 */
@Composable
expect fun rememberAccessLocalNetworkPermissionRequester(
    onResult: () -> Unit,
): (openSettingsWhenPermanentlyDenied: Boolean) -> Unit
