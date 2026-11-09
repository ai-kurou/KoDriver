package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kurou.kodriver.core.designsystem.KoDriverSpacing

/** チップのタップで開く詳細の見せ方。 */
enum class SpeechRecognitionDetailPresentation {
    /** 画面下から出るボトムシート。スマホ向け。 */
    BottomSheet,

    /** チップの上に出るポップオーバー。広い画面（デスクトップ）向け。 */
    Popover,
}

private val POPOVER_WIDTH = 360.dp
private val POPOVER_GAP = 8.dp
private val POPOVER_SHADOW_ELEVATION = 8.dp
private val DETAIL_LEVEL_GAUGE_HEIGHT = 12.dp

/**
 * 音声認識の詳細を、[presentation] に応じてボトムシートまたはポップオーバーで表示する。
 * 中身は [SpeechRecognitionDetailContent] で共通にし、外枠だけを切り替える。ポップオーバーは、
 * この関数を呼んだ位置（チップ）の上に、右端をそろえて出す。
 */
@Composable
internal fun SpeechRecognitionDetail(
    presentation: SpeechRecognitionDetailPresentation,
    uiState: SpeechRecognitionUiState,
    onEnabledChange: (Boolean) -> Unit,
    onDeviceMenuOpen: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (presentation) {
        SpeechRecognitionDetailPresentation.BottomSheet -> {
            DetailBottomSheet(onDismissRequest) {
                SpeechRecognitionDetailContent(
                    uiState = uiState,
                    onEnabledChange = onEnabledChange,
                    onDeviceMenuOpen = onDeviceMenuOpen,
                    onDeviceSelected = onDeviceSelected,
                    modifier =
                        Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = KoDriverSpacing.medium, vertical = KoDriverSpacing.small),
                    levelGaugeHeight = DETAIL_LEVEL_GAUGE_HEIGHT,
                )
            }
        }

        SpeechRecognitionDetailPresentation.Popover -> {
            DetailPopover(onDismissRequest) {
                SpeechRecognitionDetailContent(
                    uiState = uiState,
                    onEnabledChange = onEnabledChange,
                    onDeviceMenuOpen = onDeviceMenuOpen,
                    onDeviceSelected = onDeviceSelected,
                    modifier = Modifier.padding(KoDriverSpacing.medium),
                    levelGaugeHeight = DETAIL_LEVEL_GAUGE_HEIGHT,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) { content() }
}

@Composable
private fun DetailPopover(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val gapPx = with(LocalDensity.current) { POPOVER_GAP.roundToPx() }
    Popup(
        popupPositionProvider = remember(gapPx) { AboveAnchorEndPositionProvider(gapPx) },
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = Modifier.widthIn(max = POPOVER_WIDTH),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = POPOVER_SHADOW_ELEVATION,
        ) {
            content()
        }
    }
}

/** ポップオーバーをアンカー（チップ）の上に、右端をそろえて置く。画面の外へはみ出す分は内側へ寄せる。 */
internal class AboveAnchorEndPositionProvider(
    private val gapPx: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, maxX)
        val y = (anchorBounds.top - gapPx - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}
