package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

/** フラッグ読み上げで収録音声が明示的に選ばれているかを監視する。 */
class ObserveLmuWindowsFlagRecordedVoiceSelectedUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(target: LmuWindowsFlagReadoutTarget): Flow<Boolean> =
        repository.observeRecordedVoiceSelected(target)
}
