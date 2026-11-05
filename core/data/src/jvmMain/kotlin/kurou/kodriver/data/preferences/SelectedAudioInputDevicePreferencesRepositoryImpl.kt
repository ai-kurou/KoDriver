package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository

internal class SelectedAudioInputDevicePreferencesRepositoryImpl(
    private val dataStore: DataStore<SelectedAudioInputDevicePreferences>,
) : SelectedAudioInputDevicePreferencesRepository {
    override fun selectedDeviceName(): Flow<String> = dataStore.observeProperty { it.deviceName }

    override suspend fun saveSelectedDeviceName(name: String) {
        dataStore.saveProperty(name) { prefs, value -> prefs.copy(deviceName = value) }
    }
}
