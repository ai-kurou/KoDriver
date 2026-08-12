package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

// detailPane（Gt7Ps5ReadoutTyreTemperatureDetailViewModel）・Narrator（Gt7Ps5NarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val tyreTemperatureEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        ReadoutItemKey.Gt7Ps5.TyreTemperature.OverheatWarning to true,
    )

class ObserveGt7Ps5TyreTemperatureEnabledStatesUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> tyreTemperatureEnabledStateDefaults + persisted }
}
