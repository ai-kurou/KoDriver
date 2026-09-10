package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsRainPreferencesRepository

// detailPane（LmuWindowsReadoutRainDetailViewModel）・Narrator（LmuWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val rainEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.LmuWindows.Rain.Start to true,
    )

class ObserveLmuWindowsRainEnabledStatesUseCase(
    private val repository: LmuWindowsRainPreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeRainEnabledStates().map { persisted -> rainEnabledStateDefaults + persisted }
}
