package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/** ACE のフラッグごとの自由文言を永続化する。後続フラッグはメソッドを追加する。空白文言では読み上げない。 */
interface AceWindowsFlagReadoutTextPreferencesRepository {
    fun observeCheckeredFlagText(): Flow<String>

    suspend fun saveCheckeredFlagText(text: String)
}
