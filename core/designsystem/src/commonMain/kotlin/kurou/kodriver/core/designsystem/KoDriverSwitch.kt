package kurou.kodriver.core.designsystem

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 設定項目のオン・オフを切り替えるスイッチ。切り替え時は ContextClick ハプティックを発生させてから [onCheckedChange] を呼ぶ。
 */
@Composable
public fun KoDriverSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Switch(
        checked = checked,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            onCheckedChange(it)
        },
        modifier = modifier,
    )
}
