package kurou.kodriver.domain.model

/**
 * オーバーレイウィンドウの画面上の位置とサイズ。
 *
 * 単位は AWT のウィンドウ座標系（論理ピクセル）で、Compose Desktop の `WindowState` が扱う dp 値と
 * 1:1 で対応する（`WindowPosition.Absolute` / `DpSize` は `Dp.value` をそのまま
 * `java.awt.Window#setLocation` / `#setSize` に渡すため）。このためデスクトップ側で dp とピクセルの
 * 変換を行う必要はない。
 *
 * [x] / [y] が [OVERLAY_WINDOW_POSITION_UNSPECIFIED] の場合は位置が未保存であることを表し、
 * 呼び出し側は既定の表示位置（画面上部中央）を使う。
 */
data class OverlayWindowBounds(
    val x: Int = OVERLAY_WINDOW_POSITION_UNSPECIFIED,
    val y: Int = OVERLAY_WINDOW_POSITION_UNSPECIFIED,
    val width: Int = OVERLAY_WINDOW_WIDTH_DEFAULT,
    val height: Int = OVERLAY_WINDOW_HEIGHT_DEFAULT,
) {
    val isPositionSpecified: Boolean
        get() = x != OVERLAY_WINDOW_POSITION_UNSPECIFIED && y != OVERLAY_WINDOW_POSITION_UNSPECIFIED
}
