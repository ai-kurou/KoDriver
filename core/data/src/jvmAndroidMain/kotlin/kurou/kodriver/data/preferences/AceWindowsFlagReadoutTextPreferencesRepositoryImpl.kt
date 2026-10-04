package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

@Suppress("TooManyFunctions")
internal class AceWindowsFlagReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsFlagReadoutTextPreferences>,
) : AceWindowsFlagReadoutTextPreferencesRepository {
    override fun observeCheckeredFlagText(): Flow<String> = dataStore.observeProperty { it.checkeredFlagText }

    override suspend fun saveCheckeredFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(checkeredFlagText = value) }
    }

    override fun observeWhiteFlagText(): Flow<String> = dataStore.observeProperty { it.whiteFlagText }

    override suspend fun saveWhiteFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(whiteFlagText = value) }
    }

    override fun observeGreenFlagText(): Flow<String> = dataStore.observeProperty { it.greenFlagText }

    override suspend fun saveGreenFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(greenFlagText = value) }
    }

    override fun observeRedFlagText(): Flow<String> = dataStore.observeProperty { it.redFlagText }

    override suspend fun saveRedFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(redFlagText = value) }
    }

    override fun observeBlueFlagText(): Flow<String> = dataStore.observeProperty { it.blueFlagText }

    override suspend fun saveBlueFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(blueFlagText = value) }
    }

    override fun observeYellowFlagText(): Flow<String> = dataStore.observeProperty { it.yellowFlagText }

    override suspend fun saveYellowFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(yellowFlagText = value) }
    }

    override fun observeBlackFlagText(): Flow<String> = dataStore.observeProperty { it.blackFlagText }

    override suspend fun saveBlackFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(blackFlagText = value) }
    }

    override fun observeBlackWhiteFlagText(): Flow<String> = dataStore.observeProperty { it.blackWhiteFlagText }

    override suspend fun saveBlackWhiteFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(blackWhiteFlagText = value) }
    }

    override fun observeOrangeCircleFlagText(): Flow<String> = dataStore.observeProperty { it.orangeCircleFlagText }

    override suspend fun saveOrangeCircleFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(orangeCircleFlagText = value) }
    }

    override fun observeRedYellowStripesFlagText(): Flow<String> =
        dataStore.observeProperty { it.redYellowStripesFlagText }

    override suspend fun saveRedYellowStripesFlagText(text: String) {
        dataStore.saveProperty(text) { prefs, value -> prefs.copy(redYellowStripesFlagText = value) }
    }
}
