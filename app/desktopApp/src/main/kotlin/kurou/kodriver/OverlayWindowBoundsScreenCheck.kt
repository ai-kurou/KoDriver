package kurou.kodriver

import kurou.kodriver.presentation.NarratorOverlayWindowBounds
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle

/**
 * 復元したウィンドウが「掴める」と見なすために必要な、画面内に収まっている領域の最小サイズ（ピクセル）。
 */
private const val MIN_VISIBLE_WIDTH = 80
private const val MIN_VISIBLE_HEIGHT = 40

/**
 * 保存された位置が現在のモニタ構成で画面内に収まっていれば復元先の座標を、収まっていなければ `null` を返す。
 *
 * 保存時とモニタ構成が変わっている（サブモニタを外した、解像度を変えた等）と、復元した
 * オーバーレイウィンドウが画面外に配置されてドラッグで掴めなくなる。オーバーレイは
 * タイトルバーを持たずパネル自体のドラッグでしか移動できないため、位置を復元してよいのは
 * どれか一つのモニタと十分な面積が重なっている場合だけとする。
 */
internal fun NarratorOverlayWindowBounds.restorablePosition(screenBounds: List<Rectangle>): Point? {
    val windowBounds = Rectangle(x ?: return null, y ?: return null, width, height)
    val isVisible =
        screenBounds.any { screen ->
            val intersection = screen.intersection(windowBounds)
            !intersection.isEmpty &&
                intersection.width >= MIN_VISIBLE_WIDTH &&
                intersection.height >= MIN_VISIBLE_HEIGHT
        }
    return windowBounds.location.takeIf { isVisible }
}

/**
 * 現在接続されているモニタの領域を返す。ヘッドレス環境では空リストを返す（位置は復元されない）。
 */
internal fun currentScreenBounds(): List<Rectangle> =
    if (GraphicsEnvironment.isHeadless()) {
        emptyList()
    } else {
        GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .screenDevices
            .map { it.defaultConfiguration.bounds }
    }
