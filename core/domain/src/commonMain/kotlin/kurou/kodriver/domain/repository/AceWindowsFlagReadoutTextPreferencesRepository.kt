package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/** ACE のフラッグごとの自由文言を永続化する。後続フラッグはメソッドを追加する。空白文言では読み上げない。 */
@Suppress("TooManyFunctions")
interface AceWindowsFlagReadoutTextPreferencesRepository {
    fun observeCheckeredFlagText(): Flow<String>

    suspend fun saveCheckeredFlagText(text: String)

    fun observeWhiteFlagText(): Flow<String>

    suspend fun saveWhiteFlagText(text: String)

    fun observeGreenFlagText(): Flow<String>

    suspend fun saveGreenFlagText(text: String)

    fun observeRedFlagText(): Flow<String>

    suspend fun saveRedFlagText(text: String)

    fun observeBlueFlagText(): Flow<String>

    suspend fun saveBlueFlagText(text: String)

    fun observeYellowFlagText(): Flow<String>

    suspend fun saveYellowFlagText(text: String)

    fun observeBlackFlagText(): Flow<String>

    suspend fun saveBlackFlagText(text: String)

    fun observeBlackWhiteFlagText(): Flow<String>

    suspend fun saveBlackWhiteFlagText(text: String)
}
