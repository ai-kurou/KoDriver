package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository

/** 選択中のシミュレーターに対応する、音声タブの案内（読み上げ項目と語句）を購読する。シミュレーターの切り替えに追従する。 */
class ObserveSpeechCommandGuideUseCase(
    private val simulatorRepository: SimulatorPreferencesRepository,
    private val guideRepository: SpeechCommandGuideRepository,
) {
    operator fun invoke(): Flow<List<SpeechCommandGuideGroup>> =
        simulatorRepository.selectedSimulator().map { guideRepository.guideGroups(it) }
}
