package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * LMU のフラッグ読み上げ項目について、ユーザーが指定したカスタム読み上げ文言を永続化する Repository。
 *
 * 空文字は「カスタムしていない」状態を表し、その場合は収録済みWAVで読み上げる。
 */
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
