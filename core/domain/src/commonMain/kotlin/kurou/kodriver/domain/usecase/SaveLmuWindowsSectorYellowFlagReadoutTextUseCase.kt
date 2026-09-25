package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

/**
 * カスタム読み上げ文言を保存する。
 *
 * 前後の空白を除去し、空白のみの入力は未設定（空文字）として保存する。
 * UI 側でも入力を制限するが、保存値の上限は仕様値として [READOUT_CUSTOM_TEXT_MAX_LENGTH] で切り詰める。
 */
class SaveLmuWindowsSectorYellowFlagReadoutTextUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveSectorYellowFlagText(text.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH))
    }
}
