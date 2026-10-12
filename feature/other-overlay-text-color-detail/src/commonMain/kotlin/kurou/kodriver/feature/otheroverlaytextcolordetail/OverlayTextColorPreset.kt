package kurou.kodriver.feature.otheroverlaytextcolordetail

import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.Res
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_amber
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_blue
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_cyan
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_green
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_magenta
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_red
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_white
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_yellow
import org.jetbrains.compose.resources.StringResource

/** ゲーム画面用の文字色。アプリテーマに依存しないARGB値を選ぶ。 */
internal enum class OverlayTextColorPreset(
    val argb: Long,
    val label: StringResource,
) {
    AMBER(OVERLAY_TEXT_COLOR_DEFAULT, Res.string.overlay_text_color_amber),
    WHITE(0xFFFFFFFFL, Res.string.overlay_text_color_white),
    RED(0xFFFF0000L, Res.string.overlay_text_color_red),
    GREEN(0xFF00FF00L, Res.string.overlay_text_color_green),
    BLUE(0xFF0000FFL, Res.string.overlay_text_color_blue),
    CYAN(0xFF00FFFFL, Res.string.overlay_text_color_cyan),
    MAGENTA(0xFFFF00FFL, Res.string.overlay_text_color_magenta),
    YELLOW(0xFFFFFF00L, Res.string.overlay_text_color_yellow),
}
