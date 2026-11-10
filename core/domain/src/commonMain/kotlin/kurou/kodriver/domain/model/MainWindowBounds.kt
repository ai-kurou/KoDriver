package kurou.kodriver.domain.model

/**
 * デスクトップ版メインウィンドウの画面上の位置とサイズ。
 *
 * 単位は AWT のウィンドウ座標系（論理ピクセル）で、Compose Desktop の `WindowState` が扱う dp 値と
 * 1:1 で対応する（[OverlayWindowBounds] と同じ）。
 *
 * [x] / [y] が [MAIN_WINDOW_POSITION_UNSPECIFIED] の場合は位置が未保存であることを表し、
 * 呼び出し側は OS 任せの既定の表示位置を使う。
 */
data class MainWindowBounds(
    val x: Int = MAIN_WINDOW_POSITION_UNSPECIFIED,
    val y: Int = MAIN_WINDOW_POSITION_UNSPECIFIED,
    val width: Int = MAIN_WINDOW_WIDTH_DEFAULT,
    val height: Int = MAIN_WINDOW_HEIGHT_DEFAULT,
) {
    val isPositionSpecified: Boolean
        get() = x != MAIN_WINDOW_POSITION_UNSPECIFIED && y != MAIN_WINDOW_POSITION_UNSPECIFIED
}
