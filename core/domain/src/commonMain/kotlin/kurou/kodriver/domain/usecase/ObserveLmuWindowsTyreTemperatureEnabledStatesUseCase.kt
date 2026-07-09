package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

// detailPane（LmuWindowsReadoutTyreTemperatureDetailViewModel）・Narrator（LmuWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val tyreTemperatureEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = mapOf(
    ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
    ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning to true,
)

class ObserveLmuWindowsTyreTemperatureEnabledStatesUseCase(
    private val repository: LmuWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> tyreTemperatureEnabledStateDefaults + persisted }
}
