package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.acewindows.repository.AceWindowsBrakeTemperaturePreferencesRepository
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey

// 設定画面や読み上げ判定を実装するときに同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val brakeTemperatureEnabledStateDefaults: Map<ReadoutItemKey, Boolean> =
    mapOf(
        AceWindowsReadoutItemKey.BrakeTemperature.OverheatWarning to true,
    )

public class ObserveAceWindowsBrakeTemperatureEnabledStatesUseCase(
    private val repository: AceWindowsBrakeTemperaturePreferencesRepository,
) {
    public operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeEnabledStates().map { persisted -> brakeTemperatureEnabledStateDefaults + persisted }
}
