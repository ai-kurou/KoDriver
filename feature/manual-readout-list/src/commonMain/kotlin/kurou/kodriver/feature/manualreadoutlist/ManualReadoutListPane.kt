package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_help_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_hotkey_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_hotkey_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_label
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_no_data_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_no_data_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_queue_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_queue_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_readout_description
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_hint_readout_title
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_empty_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** 選択中シミュレーターの手動読み上げセット一覧を表示する。 */
@Composable
fun ManualReadoutListPane(
    simulatorId: String,
    simulatorName: String,
    modifier: Modifier = Modifier,
) {
    val viewModel: ManualReadoutListViewModel =
        koinViewModel(key = simulatorId, parameters = { parametersOf(simulatorId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ManualReadoutListPaneContent(uiState = uiState, simulatorName = simulatorName, modifier = modifier)
}

/**
 * 保存済みセットが空なら空状態を表示し、1件以上なら説明見出し行のみ表示する。
 * セットの追加と各セットの行 UI は後続 PR で実装する。
 */
@Composable
internal fun ManualReadoutListPaneContent(
    uiState: ManualReadoutListUiState,
    simulatorName: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(vertical = KoDriverSpacing.large)) {
        ManualReadoutHintRow(modifier = Modifier.padding(horizontal = KoDriverSpacing.small))
        if (uiState.sets.isEmpty()) {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualReadoutHintRow(modifier: Modifier = Modifier) {
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
        ) {
            ManualReadoutHintSheetContent(showHotkeyHint = isDesktopPlatform())
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
        modifier = modifier.padding(bottom = KoDriverSpacing.medium),
    ) {
        Text(
            text = stringResource(Res.string.manual_readout_hint_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        IconButton(
            onClick = { showHelpSheet = true },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(Res.string.manual_readout_hint_help_description),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
internal fun ManualReadoutHintSheetContent(
    showHotkeyHint: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KoDriverSpacing.large)
                .padding(bottom = KoDriverSpacing.extraLarge),
        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
    ) {
        ManualReadoutHintSheetRow(
            icon = Icons.Filled.PlayArrow,
            title = stringResource(Res.string.manual_readout_hint_readout_title),
            description = stringResource(Res.string.manual_readout_hint_readout_description),
        )
        ManualReadoutHintSheetRow(
            icon = Icons.AutoMirrored.Filled.PlaylistAdd,
            title = stringResource(Res.string.manual_readout_hint_queue_title),
            description = stringResource(Res.string.manual_readout_hint_queue_description),
        )
        ManualReadoutHintSheetRow(
            icon = Icons.Outlined.Info,
            title = stringResource(Res.string.manual_readout_hint_no_data_title),
            description = stringResource(Res.string.manual_readout_hint_no_data_description),
        )
        if (showHotkeyHint) {
            ManualReadoutHintSheetRow(
                icon = Icons.Filled.Keyboard,
                title = stringResource(Res.string.manual_readout_hint_hotkey_title),
                description = stringResource(Res.string.manual_readout_hint_hotkey_description),
            )
        }
    }
}

@Composable
private fun ManualReadoutHintSheetRow(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium)) {
        ManualReadoutHintIconBadge(icon = icon)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ManualReadoutHintIconBadge(icon: ImageVector) {
    Box(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(KoDriverSpacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp),
        )
    }
}
