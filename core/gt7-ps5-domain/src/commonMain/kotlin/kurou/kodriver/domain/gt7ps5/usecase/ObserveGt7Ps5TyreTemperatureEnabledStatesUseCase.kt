package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

// detailPane（Gt7Ps5AutoReadoutTyreTemperatureDetailViewModel）・Narrator（Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val tyreTemperatureEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        Gt7Ps5ReadoutItemKey.TyreTemperature.OverheatWarning to true,
    )

class ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> tyreTemperatureEnabledStateDefaults + persisted }
}
