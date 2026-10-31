package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverHintRow
import kurou.kodriver.core.designsystem.KoDriverHintSheetRow
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.util.isManualReadoutHotkeySupported
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
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
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_list_set_count
import org.jetbrains.compose.resources.stringResource

private const val SIMHUB_HOTKEY_SETUP_URL =
    "https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md"

@Composable
internal fun ManualReadoutHintRow(
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
