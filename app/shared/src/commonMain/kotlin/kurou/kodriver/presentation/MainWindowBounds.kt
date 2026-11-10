package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.main.rememberMainWindowBounds as rememberFeatureMainWindowBounds
import kurou.kodriver.feature.main.rememberMainWindowBoundsSaver as rememberFeatureMainWindowBoundsSaver

/**
 * メインウィンドウの位置・サイズ。位置が未保存の場合は [x] / [y] が `null` になる。
 *
 * `app:shared`・`app:desktopApp` は `:core:domain` を参照できない（`moduleGraphAssert`）ため、
 * `:core:domain` の `MainWindowBounds` ではなく、この型でウィンドウ側へ受け渡す。
 */
data class MainWindowSavedBounds(
    val x: Int?,
    val y: Int?,
    val width: Int,
    val height: Int,
)

/**
 * メインウィンドウの位置・サイズの保存値を購読する。読み込みが完了するまでは `null` を返す。
 *
 * メインウィンドウをホストする側（`app:desktopApp`）が、前回終了時の位置・サイズの復元に使う。
 */
@Composable
fun rememberMainWindowBounds(): MainWindowSavedBounds? = rememberFeatureMainWindowBounds(::MainWindowSavedBounds)

/**
 * メインウィンドウの位置・サイズを保存する関数を返す。
 *
 * メインウィンドウをホストする側（`app:desktopApp`）が、移動・リサイズの結果の保存に使う。
 */
@Composable
fun rememberMainWindowBoundsSaver(): (x: Int, y: Int, width: Int, height: Int) -> Unit =
    rememberFeatureMainWindowBoundsSaver()
