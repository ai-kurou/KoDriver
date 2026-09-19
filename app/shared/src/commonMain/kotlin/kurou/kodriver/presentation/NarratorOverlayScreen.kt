package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.feature.narratoroverlay.NarratorOverlayContent
import kurou.kodriver.feature.narratoroverlay.rememberNarratorOverlayBounds as rememberFeatureNarratorOverlayBounds
import kurou.kodriver.feature.narratoroverlay.rememberNarratorOverlayBoundsSaver as rememberFeatureNarratorOverlayBoundsSaver
import kurou.kodriver.feature.narratoroverlay.rememberNarratorOverlayVisible as rememberFeatureNarratorOverlayVisible

/**
 * NarratorOverlayScreen を提供する公開関数。
 *
 * ゲーム画面に重ねて表示する専用ウィンドウ（デスクトップアプリ、Windows / macOS / Linux 共通）にホストする、
 * 読み上げ内容オーバーレイの画面。
 */
@Composable
fun NarratorOverlayScreen(modifier: Modifier = Modifier) {
    NarratorOverlayContent(modifier = modifier)
}

/**
 * オーバーレイを表示するかどうかのユーザー設定（その他タブの「オーバーレイ設定」）を購読する。
 * 設定の読み込みが完了するまでは `null` を返す。
 *
 * オーバーレイ用ウィンドウをホストする側（`app:desktopApp`）が、ウィンドウを開くかどうかの判断に使う。
 */
@Composable
fun rememberNarratorOverlayVisible(): Boolean? = rememberFeatureNarratorOverlayVisible()

/**
 * オーバーレイウィンドウの位置・サイズ。位置が未保存の場合は [x] / [y] が `null` になる。
 *
 * `app:shared`・`app:desktopApp` は `:core:domain` を参照できない（`moduleGraphAssert`）ため、
 * `:core:domain` の `OverlayWindowBounds` ではなく、この型でウィンドウ側へ受け渡す。
 */
data class NarratorOverlayWindowBounds(
    val x: Int?,
    val y: Int?,
    val width: Int,
    val height: Int,
)

/**
 * オーバーレイウィンドウの位置・サイズの保存値を購読する。読み込みが完了するまでは `null` を返す。
 *
 * オーバーレイ用ウィンドウをホストする側（`app:desktopApp`）が、ウィンドウの初期位置・サイズに使う。
 */
@Composable
fun rememberNarratorOverlayBounds(): NarratorOverlayWindowBounds? =
    rememberFeatureNarratorOverlayBounds(::NarratorOverlayWindowBounds)

/**
 * オーバーレイウィンドウの位置・サイズを保存する関数を返す。
 *
 * オーバーレイ用ウィンドウをホストする側（`app:desktopApp`）が、移動・リサイズの結果の保存に使う。
 */
@Composable
fun rememberNarratorOverlayBoundsSaver(): (x: Int, y: Int, width: Int, height: Int) -> Unit =
    rememberFeatureNarratorOverlayBoundsSaver()
