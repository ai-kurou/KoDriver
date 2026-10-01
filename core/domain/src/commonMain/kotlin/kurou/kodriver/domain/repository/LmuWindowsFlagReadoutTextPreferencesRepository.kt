package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget

/**
 * LMU のフラッグ読み上げ項目について、ユーザーが指定したカスタム読み上げ文言を永続化する Repository。
 *
 * 空文字は「カスタムしていない」状態を表し、その場合は収録済みWAVで読み上げる。
 * 収録音声が明示的に選ばれている場合（[observeRecordedVoiceSelected]）は、文言が残っていても収録済みWAVで読み上げる。
 */
@Suppress("TooManyFunctions")
interface LmuWindowsFlagReadoutTextPreferencesRepository {
    fun observeSectorYellowFlagText(): Flow<String>

    suspend fun saveSectorYellowFlagText(text: String)

    fun observeBlueFlagText(): Flow<String>

    suspend fun saveBlueFlagText(text: String)

    fun observeFullCourseYellowFlagText(): Flow<String>

    suspend fun saveFullCourseYellowFlagText(text: String)

    fun observeRedFlagText(): Flow<String>

    suspend fun saveRedFlagText(text: String)

    fun observeRecordedVoiceSelected(target: LmuWindowsFlagReadoutTarget): Flow<Boolean>

    suspend fun saveRecordedVoiceSelected(
        target: LmuWindowsFlagReadoutTarget,
        selected: Boolean,
    )
}
