package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.PlayingEqualizer
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_item_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_no_items
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_readout_set
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_readout_set_named
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_requesting
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_tap_to_edit
import org.jetbrains.compose.resources.stringResource

/** 再生を主操作にし、長押しまたは編集モードで詳細を開くタイル。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ManualReadoutSetPlayButton(
    set: ManualReadoutSet,
    title: String,
    setNumber: Int,
    editMode: Boolean,
    selected: Boolean,
    canReadSet: Boolean,
    showHotkeyChip: Boolean,
    onReadSet: () -> Unit,
    onSetClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val enabled = editMode || canReadSet
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
                .alpha(if (enabled) 1f else 0.38f)
                .combinedClickable(
                    onClick = {
                        if (editMode) {
                            if (!selected) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onSetClick()
                        } else if (canReadSet) {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onReadSet()
                            requestCount++
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onSetClick()
                    },
                ),
        shape = MaterialTheme.shapes.large,
        color = if (editMode) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.primary,
        contentColor = if (editMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
        border = if (editMode) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp).padding(KoDriverSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall, Alignment.CenterVertically),
        ) {
            ManualReadoutPlayTileIcon(editMode, requestCount != 0, set, setNumber)
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Text(
                text = manualReadoutPlayButtonSubtitle(editMode, set.itemKeys.size),
                style = MaterialTheme.typography.bodyMedium,
            )
            ManualReadoutCategoryDots(remember(set.itemKeys) { manualReadoutCategorySummary(set.itemKeys) })
            if (showHotkeyChip && manualReadoutHotkeyLabel(set.hotkeyModifiers, set.hotkeyVirtualKeyCode) != null) {
                ManualReadoutHotkeyChip(set, playButtonLayout = true)
            }
        }
    }
}

@Composable
private fun ManualReadoutPlayTileIcon(
    editMode: Boolean,
    requesting: Boolean,
    set: ManualReadoutSet,
    setNumber: Int,
) {
    if (!editMode && requesting) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            PlayingEqualizer(stringResource(Res.string.manual_readout_list_requesting))
        }
    } else {
        Icon(
            imageVector = if (editMode) Icons.Filled.Edit else Icons.Filled.PlayArrow,
            contentDescription = if (editMode) null else manualReadoutPlayButtonDescription(set, setNumber),
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

@Composable
internal fun manualReadoutPlayButtonSubtitle(
    editMode: Boolean,
    itemCount: Int,
): String =
    when {
        editMode -> stringResource(Res.string.manual_readout_tap_to_edit)
        itemCount == 0 -> stringResource(Res.string.manual_readout_list_no_items)
        else -> stringResource(Res.string.manual_readout_list_item_count, itemCount)
    }
