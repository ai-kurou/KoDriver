package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.normalizeReadoutCustomText

/**
 * フラッグごとのカスタム読み上げ文言を [AceWindowsFlagReadoutTextKey] を指定して保存する。
 *
 * 前後の空白を除去し、空白のみの入力は未設定（空文字）として保存する。
 * 保存値の正規化は [normalizeReadoutCustomText] に従う。
 */
class SaveAceWindowsFlagReadoutTextUseCase(
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository,
) {
    suspend operator fun invoke(
        key: AceWindowsFlagReadoutTextKey,
        text: String,
    ) {
        repository.saveText(key, text.normalizeReadoutCustomText())
    }
}
