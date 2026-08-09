package kurou.kodriver.feature.readoutlist

import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.readoutItemDisplayName
import kurou.kodriver.domain.model.ReadoutItemKey

@Composable
internal fun itemDisplayName(itemId: ReadoutItemKey): String = readoutItemDisplayName(itemId.value)
