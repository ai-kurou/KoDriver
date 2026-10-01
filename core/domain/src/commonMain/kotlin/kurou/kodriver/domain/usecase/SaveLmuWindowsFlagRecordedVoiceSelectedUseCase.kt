package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

/** フラッグ読み上げで収録音声を明示的に選ぶかどうかを保存する。カスタム文言は変更しない。 */
class SaveLmuWindowsFlagRecordedVoiceSelectedUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(
        target: LmuWindowsFlagReadoutTarget,
        selected: Boolean,
    ) {
        repository.saveRecordedVoiceSelected(target, selected)
    }
}
