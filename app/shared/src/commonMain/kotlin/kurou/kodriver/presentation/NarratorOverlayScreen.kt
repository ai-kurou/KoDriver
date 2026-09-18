package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurou.kodriver.feature.narratoroverlay.NarratorOverlayContent
import kurou.kodriver.feature.narratoroverlay.rememberNarratorOverlayVisible as rememberFeatureNarratorOverlayVisible

/**
 * NarratorOverlayScreen を提供する公開関数。
 *
 * ゲーム画面に重ねて表示する専用ウィンドウ（Windows版デスクトップアプリのみ）にホストする、
 * 読み上げ内容オーバーレイの画面。
 */
@Composable
fun NarratorOverlayScreen(modifier: Modifier = Modifier) {
    NarratorOverlayContent(modifier = modifier)
}

/**
 * オーバーレイを表示するかどうかのユーザー設定（その他タブの「オーバーレイ設定」）を購読する。
 *
 * オーバーレイ用ウィンドウをホストする側（`app:desktopApp`）が、ウィンドウを開くかどうかの判断に使う。
 */
@Composable
fun rememberNarratorOverlayVisible(): Boolean = rememberFeatureNarratorOverlayVisible()
