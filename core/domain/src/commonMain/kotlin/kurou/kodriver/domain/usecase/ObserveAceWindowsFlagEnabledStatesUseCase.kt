package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.AceWindowsFlagPreferencesRepository

// detailPane（AceWindowsReadoutFlagDetailViewModel）・Narrator（AceWindowsNarratorViewModel）が
// 同じデフォルト値を参照できるよう、この一箇所にのみ定義する。
private val flagEnabledStateDefaults: Map<ReadoutItemKey, Boolean> = mapOf(
    ReadoutItemKey.AceWindows.Flag.WhiteFlag to true,
    ReadoutItemKey.AceWindows.Flag.GreenFlag to true,
    ReadoutItemKey.AceWindows.Flag.RedFlag to true,
    ReadoutItemKey.AceWindows.Flag.BlueFlag to true,
    ReadoutItemKey.AceWindows.Flag.YellowFlag to true,
    ReadoutItemKey.AceWindows.Flag.BlackFlag to true,
    ReadoutItemKey.AceWindows.Flag.BlackWhiteFlag to true,
    ReadoutItemKey.AceWindows.Flag.CheckeredFlag to true,
    ReadoutItemKey.AceWindows.Flag.OrangeCircleFlag to true,
    ReadoutItemKey.AceWindows.Flag.RedYellowStripesFlag to true,
)

class ObserveAceWindowsFlagEnabledStatesUseCase(private val repository: AceWindowsFlagPreferencesRepository) {
    operator fun invoke(): Flow<Map<ReadoutItemKey, Boolean>> =
        repository.observeFlagEnabledStates().map { persisted -> flagEnabledStateDefaults + persisted }
}
