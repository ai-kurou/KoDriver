package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.manualreadoutlist.generated.resources.Res
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_edit_mode_disable
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_edit_mode_enable
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_edit_mode_label
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_layout_play_buttons
import kurou.kodriver.feature.manualreadoutlist.generated.resources.manual_readout_layout_standard
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualReadoutLayoutSegment(
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
internal fun ManualReadoutEditButton(
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
