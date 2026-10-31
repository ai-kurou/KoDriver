package kurou.kodriver.feature.manualreadoutlist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry

/** 手動読み上げセット一覧と画面内状態を合成する冷たい Flow。 */
internal class ManualReadoutListFlow(
    sets: Flow<List<ManualReadoutSet>>,
    selectedSetId: Flow<String?>,
    playButtonLayoutEnabled: Flow<Boolean>,
    simulatorId: String,
) {
    private val canReadSets = simulatorId.canReadManualReadoutSets()
    val initialUiState = ManualReadoutListUiState(canReadSets = canReadSets)

    val uiState: Flow<ManualReadoutListUiState> =
        combine(sets, selectedSetId, playButtonLayoutEnabled) { sets, selectedId, enabled ->
            ManualReadoutListUiState(
                sets = sets,
                canReadSets = canReadSets,
                selectedSetId = selectedId?.takeIf { id -> sets.any { it.id == id } },
                playButtonLayoutEnabled = enabled,
            )
        }
}

internal fun String.canReadManualReadoutSets(): Boolean = ReadoutItemKeyRegistry.manualReadoutSpecOf(this) != null
