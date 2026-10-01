package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/** LMU のフラッグごとの自由文字列を永続化する。空白文言では本文を読み上げない。 */
interface LmuWindowsFlagReadoutTextPreferencesRepository {
    fun observeSectorYellowFlagText(): Flow<String>

    suspend fun saveSectorYellowFlagText(text: String)

    fun observeBlueFlagText(): Flow<String>

    suspend fun saveBlueFlagText(text: String)

    fun observeFullCourseYellowFlagText(): Flow<String>

    suspend fun saveFullCourseYellowFlagText(text: String)

    fun observeRedFlagText(): Flow<String>

    suspend fun saveRedFlagText(text: String)
}
