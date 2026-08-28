package kurou.kodriver.feature.readoutlist

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.readoutlist.generated.resources.Res
import kurou.kodriver.feature.readoutlist.generated.resources.ace_readout_timing_hint_description
import kurou.kodriver.feature.readoutlist.generated.resources.gt7_ps5_desktop_readout_hint_description
import org.jetbrains.compose.resources.stringResource

internal fun readoutItemStartIndex(
    isAceSelected: Boolean,
    isGt7Ps5DesktopHintShown: Boolean = false,
): Int = 1 + (if (isAceSelected) 1 else 0) + (if (isGt7Ps5DesktopHintShown) 1 else 0)

internal fun shouldShowGt7Ps5DesktopReadoutHint(selectedSimulator: Simulator): Boolean =
    selectedSimulator is Simulator.Gt7Ps5 && isDesktopPlatform()

@Composable
internal fun AceReadoutTimingHintRow(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.ace_readout_timing_hint_description),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(bottom = 12.dp),
    )
}

@Composable
internal fun Gt7Ps5DesktopReadoutHintRow(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.gt7_ps5_desktop_readout_hint_description),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 12.dp),
    )
}
