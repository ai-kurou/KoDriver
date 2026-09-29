package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

/** ブラックフラッグのカスタム読み上げ文言を保存する。 */
class SaveLmuWindowsBlackFlagReadoutTextUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveBlackFlagText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
