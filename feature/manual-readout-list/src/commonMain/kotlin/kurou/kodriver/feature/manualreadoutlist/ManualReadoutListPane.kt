package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.util.isManualReadoutHotkeySupported
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_add_set
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_empty_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_title_ace
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_title_gt7
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_title_lmu
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal const val REQUEST_FEEDBACK_MS = 1_500L

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
internal fun manualReadoutSetTitle(
    simulatorId: String,
    setNumber: Int,
): String =
    when (simulatorId) {
        "lmu_windows" -> stringResource(Res.string.manual_readout_list_set_title_lmu, setNumber)
        "gt7_ps5" -> stringResource(Res.string.manual_readout_list_set_title_gt7, setNumber)
        "ace_windows" -> stringResource(Res.string.manual_readout_list_set_title_ace, setNumber)
        else -> error("未対応のsimulatorId: $simulatorId")
    }
