package kurou.kodriver.data.repository

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.data.model.VehicleApproachPreferences
import kurou.kodriver.domain.repository.VehicleApproachPreferencesRepository

internal class VehicleApproachPreferencesRepositoryImpl(
    private val dataStore: DataStore<VehicleApproachPreferences>,
) : VehicleApproachPreferencesRepository {

    override fun observeSkipFirstLap(): Flow<Boolean> =
        dataStore.data.map { it.skipFirstLap }

    override suspend fun saveSkipFirstLap(skip: Boolean) {
        dataStore.updateData { it.copy(skipFirstLap = skip) }
    }
}
