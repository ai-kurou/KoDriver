package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsTyreTemperaturePreferencesRepository

// detailPane（AceWindowsReadoutTyreTemperatureDetailViewModel）・Narrator（AceWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val tyreTemperatureEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to true,
    )

class ObserveAceWindowsTyreTemperatureEnabledStatesUseCase(
    private val repository: AceWindowsTyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> tyreTemperatureEnabledStateDefaults + persisted }
}
