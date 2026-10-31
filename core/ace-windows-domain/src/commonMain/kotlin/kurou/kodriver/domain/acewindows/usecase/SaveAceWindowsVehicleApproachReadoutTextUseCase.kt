package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

/**
 * カスタム読み上げ文言を保存する。
 *
 * 前後の空白を除去し、空白のみの入力は未設定（空文字）として保存する。
 * 保存値の正規化は [normalizeReadoutCustomText] に従う。
 */
class SaveAceWindowsVehicleApproachReadoutTextUseCase(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveReadoutText(text.normalizeReadoutCustomText())
    }
}
