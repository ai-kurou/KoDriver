package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsFlagPreferencesRepository

// detailPane（LmuWindowsReadoutFlagDetailViewModel）・Narrator（LmuWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val flagEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        LmuWindowsReadoutItemKey.Flag.BlueFlag to true,
        LmuWindowsReadoutItemKey.Flag.SectorYellowFlag to true,
        LmuWindowsReadoutItemKey.Flag.FullCourseYellow to true,
        LmuWindowsReadoutItemKey.Flag.RedFlag to true,
    )

class ObserveLmuWindowsFlagEnabledStatesUseCase(
    private val repository: LmuWindowsFlagPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeFlagEnabledStates().map { persisted -> flagEnabledStateDefaults + persisted }
}
