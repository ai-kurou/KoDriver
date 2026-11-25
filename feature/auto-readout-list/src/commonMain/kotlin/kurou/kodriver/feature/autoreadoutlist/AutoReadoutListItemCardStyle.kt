package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import kurou.kodriver.core.designsystem.rightAlignedBackgroundImage

/** 行内の主操作領域とスイッチの数。 */
private const val AUTO_READOUT_ROW_BASE_PART_COUNT = 2

/** 行内の部品数: 主操作領域・読み上げスイッチに、開始音・キューが有効なら1つずつ加える。 */
internal fun autoReadoutRowPartCount(
    hasStartSound: Boolean,
    hasQueue: Boolean,
): Int = AUTO_READOUT_ROW_BASE_PART_COUNT + (if (hasStartSound) 1 else 0) + (if (hasQueue) 1 else 0)

/** 行内の各部品が矢印キーの移動対象か。開始音・キューは読み上げが有効なときだけ対象にする。 */
internal fun autoReadoutRowPartEnabled(
    hasStartSound: Boolean,
    hasQueue: Boolean,
    readoutEnabled: Boolean,
): List<Boolean> =
    buildList {
        add(true)
        add(true)
        if (hasStartSound) add(readoutEnabled)
        if (hasQueue) add(readoutEnabled)
    }

internal fun optionalRightAlignedBackgroundImage(
    painter: Painter?,
    fadeColor: Color,
): Modifier = if (painter != null) Modifier.rightAlignedBackgroundImage(painter, fadeColor) else Modifier
