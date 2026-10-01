package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

/** フラッグのカスタム文言と収録音声の選択状態を、1回の更新でまとめて保存する。 */
class SaveLmuWindowsFlagTextAndRecordedVoiceSelectedUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(
        target: LmuWindowsFlagReadoutTarget,
        text: String,
        selected: Boolean,
    ) {
        repository.saveTextAndRecordedVoiceSelected(target, text, selected)
    }
}
