package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository

internal class LmuWindowsFlagReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<FlagReadoutTextPreferences>,
) : LmuWindowsFlagReadoutTextPreferencesRepository {
    override fun observeSectorYellowFlagText(): Flow<String> = dataStore.observeProperty { it.sectorYellowFlagText }

    override suspend fun saveSectorYellowFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(sectorYellowFlagText = value) }
    }

    override fun observeBlueFlagText(): Flow<String> = dataStore.observeProperty { it.blueFlagText }

    override suspend fun saveBlueFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(blueFlagText = value) }
    }

    override fun observeFullCourseYellowFlagText(): Flow<String> =
        dataStore.observeProperty { it.fullCourseYellowFlagText }

    override suspend fun saveFullCourseYellowFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(fullCourseYellowFlagText = value) }
    }

    override fun observeRedFlagText(): Flow<String> = dataStore.observeProperty { it.redFlagText }

    override suspend fun saveRedFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(redFlagText = value) }
    }

    override fun observeRecordedVoiceSelected(target: LmuWindowsFlagReadoutTarget): Flow<Boolean> =
        dataStore.observeProperty { it.recordedVoiceSelected(target) }

    override suspend fun saveRecordedVoiceSelected(
        target: LmuWindowsFlagReadoutTarget,
        selected: Boolean,
    ) {
        dataStore.saveProperty(selected) { prefs, value -> prefs.withRecordedVoiceSelected(target, value) }
    }

    override suspend fun saveTextAndRecordedVoiceSelected(
        target: LmuWindowsFlagReadoutTarget,
        text: String,
        selected: Boolean,
    ) {
        dataStore.updateData { prefs -> prefs.withText(target, text).withRecordedVoiceSelected(target, selected) }
    }

    private fun FlagReadoutTextPreferences.withText(
        target: LmuWindowsFlagReadoutTarget,
        text: String,
    ): FlagReadoutTextPreferences =
        when (target) {
            LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG -> copy(sectorYellowFlagText = text)
            LmuWindowsFlagReadoutTarget.BLUE_FLAG -> copy(blueFlagText = text)
            LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW -> copy(fullCourseYellowFlagText = text)
            LmuWindowsFlagReadoutTarget.RED_FLAG -> copy(redFlagText = text)
        }

    private fun FlagReadoutTextPreferences.recordedVoiceSelected(target: LmuWindowsFlagReadoutTarget): Boolean =
        when (target) {
            LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG -> sectorYellowFlagRecordedVoiceSelected
            LmuWindowsFlagReadoutTarget.BLUE_FLAG -> blueFlagRecordedVoiceSelected
            LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW -> fullCourseYellowFlagRecordedVoiceSelected
            LmuWindowsFlagReadoutTarget.RED_FLAG -> redFlagRecordedVoiceSelected
        }

    private fun FlagReadoutTextPreferences.withRecordedVoiceSelected(
        target: LmuWindowsFlagReadoutTarget,
        selected: Boolean,
    ): FlagReadoutTextPreferences =
        when (target) {
            LmuWindowsFlagReadoutTarget.SECTOR_YELLOW_FLAG -> copy(sectorYellowFlagRecordedVoiceSelected = selected)
            LmuWindowsFlagReadoutTarget.BLUE_FLAG -> copy(blueFlagRecordedVoiceSelected = selected)
            LmuWindowsFlagReadoutTarget.FULL_COURSE_YELLOW -> copy(fullCourseYellowFlagRecordedVoiceSelected = selected)
            LmuWindowsFlagReadoutTarget.RED_FLAG -> copy(redFlagRecordedVoiceSelected = selected)
        }
}
