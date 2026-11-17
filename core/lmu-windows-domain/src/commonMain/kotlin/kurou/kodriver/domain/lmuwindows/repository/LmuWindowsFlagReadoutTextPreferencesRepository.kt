package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow

/** LMU のフラッグごとの自由文字列を永続化する。空白文言では本文を読み上げない。 */
public interface LmuWindowsFlagReadoutTextPreferencesRepository {
    public suspend fun resetToDefault()

    public fun observeSectorYellowFlagText(): Flow<String>

    public suspend fun saveSectorYellowFlagText(text: String)

    public fun observeBlueFlagText(): Flow<String>

    public suspend fun saveBlueFlagText(text: String)

    public fun observeFullCourseYellowFlagText(): Flow<String>

    public suspend fun saveFullCourseYellowFlagText(text: String)

    public fun observeRedFlagText(): Flow<String>

    public suspend fun saveRedFlagText(text: String)
}
