package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository

internal class AceWindowsFlagReadoutTextPreferencesRepositoryImpl(
    private val dataStore: DataStore<AceWindowsFlagReadoutTextPreferences>,
) : AceWindowsFlagReadoutTextPreferencesRepository {
    override fun observeText(key: AceWindowsFlagReadoutTextKey): Flow<String> =
        dataStore.observeProperty { prefs ->
            when (key) {
                AceWindowsFlagReadoutTextKey.CHECKERED -> prefs.checkeredFlagText
                AceWindowsFlagReadoutTextKey.WHITE -> prefs.whiteFlagText
                AceWindowsFlagReadoutTextKey.GREEN -> prefs.greenFlagText
                AceWindowsFlagReadoutTextKey.RED -> prefs.redFlagText
                AceWindowsFlagReadoutTextKey.BLUE -> prefs.blueFlagText
                AceWindowsFlagReadoutTextKey.YELLOW -> prefs.yellowFlagText
                AceWindowsFlagReadoutTextKey.BLACK -> prefs.blackFlagText
                AceWindowsFlagReadoutTextKey.BLACK_WHITE -> prefs.blackWhiteFlagText
                AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE -> prefs.orangeCircleFlagText
                AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES -> prefs.redYellowStripesFlagText
            }
        }

    override suspend fun saveText(
        key: AceWindowsFlagReadoutTextKey,
        text: String,
    ) {
        dataStore.saveProperty(text) { prefs, value ->
            when (key) {
                AceWindowsFlagReadoutTextKey.CHECKERED -> prefs.copy(checkeredFlagText = value)
                AceWindowsFlagReadoutTextKey.WHITE -> prefs.copy(whiteFlagText = value)
                AceWindowsFlagReadoutTextKey.GREEN -> prefs.copy(greenFlagText = value)
                AceWindowsFlagReadoutTextKey.RED -> prefs.copy(redFlagText = value)
                AceWindowsFlagReadoutTextKey.BLUE -> prefs.copy(blueFlagText = value)
                AceWindowsFlagReadoutTextKey.YELLOW -> prefs.copy(yellowFlagText = value)
                AceWindowsFlagReadoutTextKey.BLACK -> prefs.copy(blackFlagText = value)
                AceWindowsFlagReadoutTextKey.BLACK_WHITE -> prefs.copy(blackWhiteFlagText = value)
                AceWindowsFlagReadoutTextKey.ORANGE_CIRCLE -> prefs.copy(orangeCircleFlagText = value)
                AceWindowsFlagReadoutTextKey.RED_YELLOW_STRIPES -> prefs.copy(redYellowStripesFlagText = value)
            }
        }
    }
}
