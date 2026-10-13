package kurou.kodriver.feature.autoreadoutlist

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.readoutItemDisplayName
import kurou.kodriver.domain.model.ReadoutItemKey

@Composable
internal fun autoReadoutItemDisplayName(itemId: ReadoutItemKey): String = readoutItemDisplayName(itemId.value)
