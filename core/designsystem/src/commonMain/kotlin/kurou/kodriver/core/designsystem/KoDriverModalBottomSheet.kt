package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp

/**
 * 呼び出し元の [LocalHapticFeedback] を内容へ引き継ぐ ModalBottomSheet。
 *
 * Android ではシートが別の ComposeView になり LocalHapticFeedback が上書きされるため、
 * ハプティックのオフ設定を効かせるには ModalBottomSheet の外側で読んだ値を内容へ渡す必要がある。
 * 振動はこの部品自身では発生させない。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun KoDriverModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
    ) {
        // Android ではシートが別の ComposeView になり LocalHapticFeedback が上書きされるため、呼び出し元の値を引き継ぐ。
        CompositionLocalProvider(LocalHapticFeedback provides haptic) {
            content()
        }
    }
}
