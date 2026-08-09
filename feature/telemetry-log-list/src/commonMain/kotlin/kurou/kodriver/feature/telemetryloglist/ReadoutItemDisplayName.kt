package kurou.kodriver.feature.telemetryloglist

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.core.designsystem.readoutItemDisplayName as designSystemReadoutItemDisplayName

@Composable
internal fun readoutItemDisplayName(readoutItemKey: ReadoutItemKey): String =
    designSystemReadoutItemDisplayName(readoutItemKey.value)
