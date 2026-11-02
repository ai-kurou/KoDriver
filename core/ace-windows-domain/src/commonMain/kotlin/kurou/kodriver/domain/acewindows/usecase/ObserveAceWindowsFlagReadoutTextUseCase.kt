package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository

/** フラッグごとのカスタム読み上げ文言を [AceWindowsFlagReadoutTextKey] を指定して取得する。 */
class ObserveAceWindowsFlagReadoutTextUseCase(
    private val repository: AceWindowsFlagReadoutTextPreferencesRepository,
) {
    operator fun invoke(key: AceWindowsFlagReadoutTextKey): Flow<String> = repository.observeText(key)
}
