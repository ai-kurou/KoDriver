package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

/**
 * カスタム読み上げ文言を保存する。
 *
 * 前後の空白を除去し、空白のみの入力は未設定（空文字）として保存する。
 * 保存値の正規化は [normalizeReadoutCustomText] に従う。
 */
class SaveLmuWindowsFullCourseYellowFlagReadoutTextUseCase(
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(text: String) {
        repository.saveFullCourseYellowFlagText(text.normalizeReadoutCustomText())
    }
}
