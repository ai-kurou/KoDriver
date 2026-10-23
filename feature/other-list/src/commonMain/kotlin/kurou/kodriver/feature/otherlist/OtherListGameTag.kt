package kurou.kodriver.feature.otherlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.koDriverTagTextStyle
import kurou.kodriver.core.designsystem.simulatorShortName
import kurou.kodriver.domain.model.Simulator

/**
 * [simulators] のゲーム名タグを指定された順に並べ、その右に詳細へ進む矢印を表示する。
 * 対象ゲームは最大2件の固定リストのため、ImmutableList化のコストに見合わない。
 */
@Suppress("UnstableCollections")
@Composable
internal fun OtherListGameTagsWithChevron(
    simulators: List<Simulator>,
    isSelected: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        simulators.forEach { simulator ->
            OtherListGameTag(simulator = simulator, isSelected = isSelected)
        }
        Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null)
    }
}

/**
 * OtherList の項目が対象とするゲーム名を表示する小さなタグ。選択中の項目では背景に埋もれないよう配色を切り替える。
 */
@Composable
internal fun OtherListGameTag(
    simulator: Simulator,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondaryContainer
    Text(
        text = simulatorShortName(simulator.id),
        style = koDriverTagTextStyle(),
        color = textColor,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        modifier =
            modifier
                .requiredWidth(56.dp)
                .background(backgroundColor, MaterialTheme.shapes.small)
                .padding(vertical = KoDriverSpacing.extraSmall),
    )
}
