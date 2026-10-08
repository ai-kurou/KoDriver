package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey

/** ACE のフラッグごとの自由文言を永続化する。空白文言では読み上げない。 */
interface AceWindowsFlagReadoutTextPreferencesRepository {
    fun observeText(key: AceWindowsFlagReadoutTextKey): Flow<String>

    suspend fun saveText(
        key: AceWindowsFlagReadoutTextKey,
        text: String,
    )
}
