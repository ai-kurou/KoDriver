package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kurou.kodriver.core.designsystem.KoDriverDestructiveConfirmDialog
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.PlayingEqualizer
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_cancel
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete_body
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete_confirm
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_requesting
import org.jetbrains.compose.resources.stringResource

/** セット名・カテゴリ別件数・対応環境のホットキーと読み上げボタンを表示する。 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ManualReadoutSetRow(
    setNumber: Int,
    simulatorId: String,
    set: ManualReadoutSet,
    showHotkeyChip: Boolean,
    canReadSet: Boolean,
    onReadSet: () -> Unit,
    onSetClick: () -> Unit,
    onDeleteSet: () -> Unit,
) {
    val categories = remember(set.itemKeys) { manualReadoutCategorySummary(set.itemKeys) }
    val stripeColor =
        categories.counts
            .firstOrNull()
            ?.first
            ?.let { manualReadoutCategoryColor(it) }
            ?: MaterialTheme.colorScheme.outlineVariant
    val haptic = LocalHapticFeedback.current
    var requestCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(requestCount) {
        if (requestCount != 0) {
            delay(REQUEST_FEEDBACK_MS)
            requestCount = 0
        }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var longPressPosition by remember { mutableStateOf(Offset.Unspecified) }
    var pointerDownPosition by remember { mutableStateOf(Offset.Unspecified) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ManualReadoutListDeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                onDeleteSet()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
    ElevatedCard(
        modifier =
            Modifier
                .padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        pointerDownPosition = down.position
                        waitForUpOrCancellation()
                        pointerDownPosition = Offset.Unspecified
                    }
                }.combinedClickable(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onSetClick()
                    },
                    onLongClick = {
                        longPressPosition = pointerDownPosition
                        pointerDownPosition = Offset.Unspecified
                        menuExpanded = true
                    },
                ),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        DropdownMenuPopup(
            expanded = menuExpanded,
            onDismissRequest = {
                menuExpanded = false
            },
            popupPositionProvider =
                remember(longPressPosition) {
                    ManualReadoutMenuPositionProvider(longPressPosition)
                },
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MenuDefaults.shape,
                tonalElevation = MenuDefaults.TonalElevation,
                shadowElevation = MenuDefaults.ShadowElevation,
            ) {
                Column(modifier = Modifier.widthIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(Res.string.manual_readout_list_delete),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            showDeleteDialog = true
                        },
                    )
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .drawBehind { drawRect(stripeColor, size = size.copy(width = 5.dp.toPx())) }
                    .padding(
                        start = KoDriverSpacing.large,
                        end = KoDriverSpacing.large,
                        top = KoDriverSpacing.medium,
                        bottom = KoDriverSpacing.medium,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                Text(
                    text = set.name.ifBlank { manualReadoutSetTitle(simulatorId, setNumber) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ManualReadoutCategoryChips(categories, set.itemKeys.size)
                if (showHotkeyChip) {
                    ManualReadoutHotkeyChip(set)
                }
            }
            FilledIconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onReadSet()
                    requestCount++
                },
                enabled = canReadSet,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                colors =
                    IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            ) {
                if (requestCount != 0) {
                    PlayingEqualizer(stringResource(Res.string.manual_readout_list_requesting))
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = manualReadoutPlayButtonDescription(set, setNumber),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal class ManualReadoutMenuPositionProvider(
    private val anchor: Offset,
) : DropdownMenuPopupPositionProvider {
    override val transformOrigin: TransformOrigin = TransformOrigin(0.5f, 0.5f)

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x =
            if (anchor != Offset.Unspecified) {
                anchorBounds.left + anchor.x.toInt()
            } else {
                anchorBounds.left + anchorBounds.width / 2
            }
        val y =
            if (anchor != Offset.Unspecified) {
                anchorBounds.top + anchor.y.toInt()
            } else {
                anchorBounds.top + anchorBounds.height / 2
            }
        val preferredY = if (y + popupContentSize.height > windowSize.height) y - popupContentSize.height else y
        val preferredX = if (layoutDirection == LayoutDirection.Ltr) x else x - popupContentSize.width
        return IntOffset(
            x = preferredX.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)),
            y = preferredY.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)),
        )
    }
}

/** 一覧からのセット削除を確認する。 */
@Composable
internal fun ManualReadoutListDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KoDriverDestructiveConfirmDialog(
        title = stringResource(Res.string.manual_readout_list_delete_title),
        body = stringResource(Res.string.manual_readout_list_delete_body),
        confirmLabel = stringResource(Res.string.manual_readout_list_delete_confirm),
        dismissLabel = stringResource(Res.string.manual_readout_list_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
