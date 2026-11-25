package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kurou.kodriver.core.designsystem.KoDriverCollapsibleVisibility
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.PlayingEqualizer
import kurou.kodriver.core.designsystem.fullBackgroundImages
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_item_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_no_items
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_readout_set
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_readout_set_named
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_requesting
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 再生を主操作にし、長押しで「編集」「削除」メニューを開くタイル。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ManualReadoutSetPlayButton(
    set: ManualReadoutSet,
    title: String,
    setNumber: Int,
    canReadSet: Boolean,
    showHotkeyChip: Boolean,
    onReadSet: () -> Unit,
    onSetClick: () -> Unit,
    onDeleteSet: () -> Unit,
    focusRequester: FocusRequester,
) {
    val haptic = LocalHapticFeedback.current
    val categories = remember(set.itemKeys) { manualReadoutCategorySummary(set.itemKeys) }
    val backgroundPainters =
        categories.counts.map { (category, _) -> painterResource(manualReadoutCategoryBackground(category).drawable) }
    var menuExpanded by remember { mutableStateOf(false) }
    var requestCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(requestCount) {
        if (requestCount != 0) {
            delay(REQUEST_FEEDBACK_MS)
            requestCount = 0
        }
    }
    Surface(
        modifier =
            Modifier
                .alpha(if (canReadSet) 1f else 0.38f)
                .focusRequester(focusRequester)
                .combinedClickable(
                    onClick = {
                        if (canReadSet) {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onReadSet()
                            requestCount++
                        }
                    },
                    onLongClick = { menuExpanded = true },
                ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Box {
            ManualReadoutSetMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                onEdit = onSetClick,
                onDelete = onDeleteSet,
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 104.dp)
                        .fullBackgroundImages(backgroundPainters, MaterialTheme.colorScheme.primary)
                        .padding(KoDriverSpacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall, Alignment.CenterVertically),
            ) {
                ManualReadoutPlayTileIcon(requestCount != 0, set, setNumber)
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                ManualReadoutPlayTileCategoryChips(categories, set.itemKeys.size)
                val hasHotkeyLabel = manualReadoutHotkeyLabel(set.hotkeyModifiers, set.hotkeyVirtualKeyCode) != null
                // ホットキー解除後の退場アニメーション中に「未割り当て」チップが出ないよう、最後に割り当て済みだったセットを保つ。
                var lastAssignedSet by remember { mutableStateOf(set) }
                if (hasHotkeyLabel) lastAssignedSet = set
                KoDriverCollapsibleVisibility(visible = showHotkeyChip && hasHotkeyLabel) {
                    ManualReadoutHotkeyChip(lastAssignedSet, playButtonLayout = true)
                }
            }
        }
    }
}

@Composable
private fun ManualReadoutPlayTileIcon(
    requesting: Boolean,
    set: ManualReadoutSet,
    setNumber: Int,
) {
    if (requesting) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            PlayingEqualizer(stringResource(Res.string.manual_readout_list_requesting))
        }
    } else {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = manualReadoutPlayButtonDescription(set, setNumber),
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
internal fun manualReadoutPlayButtonDescription(
    set: ManualReadoutSet,
    setNumber: Int,
): String =
    if (set.name.isBlank()) {
        stringResource(Res.string.manual_readout_list_readout_set, setNumber)
    } else {
        stringResource(Res.string.manual_readout_list_readout_set_named, set.name)
    }

/** タイル上のカテゴリ表示。背景画像の上でも読めるよう、前面色を薄く重ねたチップに「アイコン + カテゴリ名」を出す。 */
@Composable
private fun ManualReadoutPlayTileCategoryChips(
    summary: ManualReadoutCategorySummary,
    itemCount: Int,
) {
    val counts = summary.counts
    if (counts.isEmpty()) {
        Text(
            text =
                if (itemCount == 0) {
                    stringResource(Res.string.manual_readout_list_no_items)
                } else {
                    stringResource(Res.string.manual_readout_list_item_count, itemCount)
                },
            style = MaterialTheme.typography.bodyMedium,
        )
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
        ) {
            counts.forEach { (category, _) ->
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = LocalContentColor.current.copy(alpha = PLAY_TILE_CHIP_ALPHA),
                    contentColor = LocalContentColor.current,
                ) {
                    Row(
                        modifier = Modifier.padding(KoDriverSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                    ) {
                        Icon(
                            imageVector = manualReadoutCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = manualReadoutCategoryLabel(category),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

private const val PLAY_TILE_CHIP_ALPHA = 0.18f
