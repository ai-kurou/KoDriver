package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.DropdownMenuPopupPositionProvider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kurou.kodriver.core.designsystem.KoDriverDestructiveConfirmDialog
import kurou.kodriver.core.designsystem.KoDriverHintRow
import kurou.kodriver.core.designsystem.KoDriverHintSheetRow
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.PlayingEqualizer
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.util.isManualReadoutHotkeySupported
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_edit_mode_disable
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_edit_mode_enable
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_edit_mode_label
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_delete_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_delete_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_edit_mode_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_edit_mode_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_help_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_hotkey_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_hotkey_guide_link
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_hotkey_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_label
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_layout_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_layout_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_no_data_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_no_data_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_queue_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_queue_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_readout_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_readout_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_layout_play_buttons
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_layout_standard
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_add_set
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_cancel
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete_body
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete_confirm
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_delete_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_empty_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_hotkey_unassigned
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_item_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_no_items
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_readout_set
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_readout_set_named
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_requesting
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_count
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_title_ace
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_title_gt7
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_title_lmu
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_tap_to_edit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val SIMHUB_HOTKEY_SETUP_URL =
    "https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md"

private const val REQUEST_FEEDBACK_MS = 1_500L

/** 選択中シミュレーターの手動読み上げセット一覧を表示する。 */
@Composable
fun ManualReadoutListPane(
    simulatorId: String,
    simulatorName: String,
    modifier: Modifier = Modifier,
    onSetClick: (String) -> Unit = {},
) {
    val viewModel: ManualReadoutListViewModel =
        koinViewModel(key = simulatorId, parameters = { parametersOf(simulatorId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ManualReadoutListPaneContent(
        uiState = uiState,
        simulatorId = simulatorId,
        simulatorName = simulatorName,
        onAddSet = viewModel::addSet,
        onReadSet = viewModel::readSet,
        onDeleteSet = viewModel::deleteSet,
        onTogglePlayButtonLayout = viewModel::onPlayButtonLayoutToggle,
        onSetClick = onSetClick,
        modifier = modifier,
    )
}

/**
 * 標準の行一覧と、再生・編集を切り替えられる2列の再生ボタン一覧を表示する。
 */
@Composable
internal fun ManualReadoutListPaneContent(
    uiState: ManualReadoutListUiState,
    simulatorId: String,
    simulatorName: String,
    onAddSet: () -> Unit,
    onReadSet: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSetClick: (String) -> Unit = {},
    onDeleteSet: (String) -> Unit = {},
    onTogglePlayButtonLayout: () -> Unit = {},
    showHotkeyChip: Boolean = isManualReadoutHotkeySupported(),
    listState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val haptic = LocalHapticFeedback.current
    var editMode by remember { mutableStateOf(false) }
    var additionBaseline by remember(simulatorId) { mutableStateOf<Set<String>?>(null) }
    val enteredSetIds = remember(simulatorId) { mutableSetOf<String>() }
    val addSet = {
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        additionBaseline = uiState.sets.map { it.id }.toSet()
        onAddSet()
    }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
    val entryOffset = with(LocalDensity.current) { 12.dp.roundToPx() }
    AnimatedVisibility(
        visibleState = visibleState,
        enter =
            slideInVertically(
                initialOffsetY = { entryOffset },
                animationSpec = tween(durationMillis = 200),
            ) + fadeIn(animationSpec = tween(durationMillis = 200)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(vertical = KoDriverSpacing.large)) {
                ManualReadoutHintRow(
                    setCount = uiState.sets.size,
                    showSimHubGuideLink = simulatorId != Simulator.Gt7Ps5.id,
                    playButtonLayoutEnabled = uiState.playButtonLayoutEnabled,
                    onTogglePlayButtonLayout = {
                        editMode = false
                        onTogglePlayButtonLayout()
                    },
                    editMode = editMode,
                    onToggleEditMode = { editMode = !editMode },
                    modifier = Modifier.padding(horizontal = KoDriverSpacing.small),
                )
                if (uiState.playButtonLayoutEnabled) {
                    val tileHeightsPx = remember { mutableStateMapOf<String, Int>() }
                    val density = LocalDensity.current
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding =
                            PaddingValues(
                                start = KoDriverSpacing.small,
                                end = KoDriverSpacing.small,
                                bottom = 56.dp + KoDriverSpacing.large,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                    ) {
                        itemsIndexed(uiState.sets, key = { _, set -> set.id }) { index, set ->
                            val setNumber = set.number.takeIf { it > 0 } ?: (index + 1)
                            ManualReadoutSetEntry(
                                animate = additionBaseline?.let { set.id !in it } == true,
                                setId = set.id,
                                enteredSetIds = enteredSetIds,
                                modifier =
                                    Modifier
                                        .onSizeChanged { tileHeightsPx[set.id] = it.height }
                                        .animateItem(
                                            fadeInSpec = null,
                                            placementSpec = tween(200),
                                            fadeOutSpec = null,
                                        ),
                            ) {
                                ManualReadoutSetPlayButton(
                                    set = set,
                                    title = set.name.ifBlank { manualReadoutSetTitle(simulatorId, setNumber) },
                                    setNumber = setNumber,
                                    editMode = editMode,
                                    selected = uiState.selectedSetId == set.id,
                                    canReadSet = uiState.canReadSets && set.itemKeys.isNotEmpty(),
                                    showHotkeyChip = showHotkeyChip,
                                    onReadSet = { onReadSet(set.id) },
                                    onSetClick = { onSetClick(set.id) },
                                )
                            }
                        }
                        item(key = "add-set") {
                            val tallestTileHeightPx = uiState.sets.maxOfOrNull { tileHeightsPx[it.id] ?: 0 } ?: 0
                            Surface(
                                modifier =
                                    Modifier.animateItem(
                                        fadeInSpec = null,
                                        placementSpec = tween(200),
                                        fadeOutSpec = null,
                                    ),
                                onClick = addSet,
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = maxOf(104.dp, with(density) { tallestTileHeightPx.toDp() }))
                                            .padding(KoDriverSpacing.medium),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement =
                                        Arrangement.spacedBy(KoDriverSpacing.small, Alignment.CenterVertically),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Text(stringResource(Res.string.manual_readout_list_add_set))
                                }
                            }
                        }
                    }
                } else if (uiState.sets.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxSize().padding(KoDriverSpacing.extraLarge),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.manual_readout_list_empty_title, simulatorName),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 56.dp + KoDriverSpacing.large),
                    ) {
                        itemsIndexed(uiState.sets, key = { _, set -> set.id }) { index, set ->
                            ManualReadoutSetEntry(
                                animate = additionBaseline?.let { set.id !in it } == true,
                                setId = set.id,
                                enteredSetIds = enteredSetIds,
                                modifier =
                                    Modifier.animateItem(
                                        fadeInSpec = null,
                                        placementSpec = tween(200),
                                        fadeOutSpec = null,
                                    ),
                            ) {
                                ManualReadoutSetRow(
                                    setNumber = set.number.takeIf { it > 0 } ?: (index + 1),
                                    simulatorId = simulatorId,
                                    set = set,
                                    showHotkeyChip = showHotkeyChip,
                                    canReadSet = uiState.canReadSets,
                                    onReadSet = { onReadSet(set.id) },
                                    onSetClick = { onSetClick(set.id) },
                                    onDeleteSet = { onDeleteSet(set.id) },
                                )
                            }
                        }
                    }
                }
            }
            if (!uiState.playButtonLayoutEnabled) {
                ExtendedFloatingActionButton(
                    onClick = addSet,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(Res.string.manual_readout_list_add_set),
                        )
                    },
                    text = { Text(stringResource(Res.string.manual_readout_list_add_set)) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(KoDriverSpacing.large),
                )
            }
        }
    }
}

/** 追加操作で増えたセットだけを展開し、スクロールや表示切替では再生しない。 */
@Composable
private fun ManualReadoutSetEntry(
    animate: Boolean,
    setId: String,
    enteredSetIds: MutableSet<String>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val visibleState =
        remember(setId) {
            MutableTransitionState(!(animate && enteredSetIds.add(setId))).apply { targetState = true }
        }
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = fadeIn(tween(180)) + expandVertically(tween(220), expandFrom = Alignment.Top),
    ) {
        content()
    }
}

@Composable
private fun manualReadoutSetTitle(
    simulatorId: String,
    setNumber: Int,
): String =
    when (simulatorId) {
        "lmu_windows" -> stringResource(Res.string.manual_readout_list_set_title_lmu, setNumber)
        "gt7_ps5" -> stringResource(Res.string.manual_readout_list_set_title_gt7, setNumber)
        "ace_windows" -> stringResource(Res.string.manual_readout_list_set_title_ace, setNumber)
        else -> error("未対応のsimulatorId: $simulatorId")
    }

/** 再生を主操作にし、長押しまたは編集モードで詳細を開くタイル。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ManualReadoutSetPlayButton(
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
private fun manualReadoutPlayButtonDescription(
    set: ManualReadoutSet,
    setNumber: Int,
): String =
    if (set.name.isBlank()) {
        stringResource(Res.string.manual_readout_list_readout_set, setNumber)
    } else {
        stringResource(Res.string.manual_readout_list_readout_set_named, set.name)
    }

@Composable
private fun manualReadoutPlayButtonSubtitle(
    editMode: Boolean,
    itemCount: Int,
): String =
    when {
        editMode -> stringResource(Res.string.manual_readout_tap_to_edit)
        itemCount == 0 -> stringResource(Res.string.manual_readout_list_no_items)
        else -> stringResource(Res.string.manual_readout_list_item_count, itemCount)
    }

/** セット名・カテゴリ別件数・対応環境のホットキーと読み上げボタンを表示する。 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ManualReadoutSetRow(
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

/** 行の操作を妨げない、表示専用のホットキーチップ。 */
@Composable
private fun ManualReadoutHotkeyChip(
    set: ManualReadoutSet,
    playButtonLayout: Boolean = false,
) {
    val label = manualReadoutHotkeyLabel(set.hotkeyModifiers, set.hotkeyVirtualKeyCode)
    val borderColor = if (label == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.outline
    Surface(
        shape = MaterialTheme.shapes.small,
        color =
            if (playButtonLayout) {
                MaterialTheme.colorScheme.primaryContainer
            } else if (label == null) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
        contentColor =
            if (playButtonLayout) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else if (label == null) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            },
        border = BorderStroke(1.dp, borderColor),
    ) {
        Text(
            text = label ?: stringResource(Res.string.manual_readout_list_hotkey_unassigned),
            style = MaterialTheme.typography.labelSmall,
            modifier =
                Modifier
                    .drawBehind {
                        if (label != null) {
                            drawLine(borderColor, Offset(0f, size.height), Offset(size.width, size.height), 3.dp.toPx())
                        }
                    }.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
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

@Composable
private fun ManualReadoutHintRow(
    setCount: Int,
    showSimHubGuideLink: Boolean,
    editMode: Boolean,
    onToggleEditMode: () -> Unit,
    playButtonLayoutEnabled: Boolean,
    onTogglePlayButtonLayout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Column(modifier = modifier) {
        KoDriverHintRow(
            label = stringResource(Res.string.manual_readout_hint_label),
            helpContentDescription = stringResource(Res.string.manual_readout_hint_help_description),
            trailingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (playButtonLayoutEnabled) {
                        ManualReadoutEditButton(editMode, onToggleEditMode)
                    }
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                        Row(modifier = Modifier.selectableGroup()) {
                            ManualReadoutLayoutSegment(
                                playButtonLayout = false,
                                selected = !playButtonLayoutEnabled,
                                onClick = onTogglePlayButtonLayout,
                            )
                            ManualReadoutLayoutSegment(
                                playButtonLayout = true,
                                selected = playButtonLayoutEnabled,
                                onClick = onTogglePlayButtonLayout,
                            )
                        }
                    }
                }
            },
        ) {
            ManualReadoutHintSheetContent(
                showHotkeyHint = isManualReadoutHotkeySupported(),
                showSimHubGuideLink = showSimHubGuideLink,
                onOpenHotkeyGuide = { uriHandler.openUri(SIMHUB_HOTKEY_SETUP_URL) },
            )
        }
        if (setCount > 0) {
            Text(
                text = stringResource(Res.string.manual_readout_list_set_count, setCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = KoDriverSpacing.small),
            )
        }
    }
}

@Composable
private fun ManualReadoutLayoutSegment(
    playButtonLayout: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier =
            Modifier
                .size(width = 36.dp, height = 32.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = {
                        if (!selected) {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onClick()
                        }
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (playButtonLayout) Icons.Filled.GridView else Icons.AutoMirrored.Filled.ViewList,
            contentDescription =
                stringResource(
                    if (playButtonLayout) {
                        Res.string.manual_readout_layout_play_buttons
                    } else {
                        Res.string.manual_readout_layout_standard
                    },
                ),
            modifier = Modifier.size(20.dp),
            tint =
                if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

@Composable
private fun ManualReadoutEditButton(
    editMode: Boolean,
    onClick: () -> Unit,
) {
    val description =
        stringResource(
            if (editMode) Res.string.manual_readout_edit_mode_disable else Res.string.manual_readout_edit_mode_enable,
        )
    Surface(
        onClick = onClick,
        modifier = Modifier.height(32.dp).semantics(mergeDescendants = true) { contentDescription = description },
        shape = CircleShape,
        color =
            if (editMode) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            },
        contentColor =
            if (editMode) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    ) {
        Row(
            modifier = Modifier.heightIn(min = 32.dp).padding(horizontal = KoDriverSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(stringResource(Res.string.manual_readout_edit_mode_label), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun ManualReadoutHintSheetContent(
    showHotkeyHint: Boolean,
    modifier: Modifier = Modifier,
    showSimHubGuideLink: Boolean = true,
    onOpenHotkeyGuide: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KoDriverSpacing.large)
                .padding(bottom = KoDriverSpacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
    ) {
        KoDriverHintSheetRow(
            icon = Icons.Filled.PlayArrow,
            title = stringResource(Res.string.manual_readout_hint_readout_title),
            description = stringResource(Res.string.manual_readout_hint_readout_description),
        )
        KoDriverHintSheetRow(
            icon = Icons.Filled.GridView,
            title = stringResource(Res.string.manual_readout_hint_layout_title),
            description = stringResource(Res.string.manual_readout_hint_layout_description),
        )
        KoDriverHintSheetRow(
            icon = Icons.Filled.Edit,
            title = stringResource(Res.string.manual_readout_hint_edit_mode_title),
            description = stringResource(Res.string.manual_readout_hint_edit_mode_description),
        )
        KoDriverHintSheetRow(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            title = stringResource(Res.string.manual_readout_hint_queue_title),
            description = stringResource(Res.string.manual_readout_hint_queue_description),
        )
        KoDriverHintSheetRow(
            icon = Icons.Outlined.Info,
            title = stringResource(Res.string.manual_readout_hint_no_data_title),
            description = stringResource(Res.string.manual_readout_hint_no_data_description),
        )
        KoDriverHintSheetRow(
            icon = Icons.Filled.Delete,
            title = stringResource(Res.string.manual_readout_hint_delete_title),
            description = stringResource(Res.string.manual_readout_hint_delete_description),
        )
        if (showHotkeyHint) {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall)) {
                KoDriverHintSheetRow(
                    icon = Icons.Filled.Keyboard,
                    title = stringResource(Res.string.manual_readout_hint_hotkey_title),
                    description = stringResource(Res.string.manual_readout_hint_hotkey_description),
                )
                if (showSimHubGuideLink) {
                    // KoDriverHintSheetRow のアイコン幅（24dp + 左右の余白）と行間隔に揃える。
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                        modifier =
                            Modifier
                                .padding(start = 24.dp + KoDriverSpacing.small * 2 + KoDriverSpacing.medium)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    onOpenHotkeyGuide()
                                },
                    ) {
                        Text(
                            text = stringResource(Res.string.manual_readout_hint_hotkey_guide_link),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}
