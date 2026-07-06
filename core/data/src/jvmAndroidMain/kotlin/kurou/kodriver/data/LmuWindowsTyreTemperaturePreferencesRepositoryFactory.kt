package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsTyreTemperaturePreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsTyreTemperaturePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

fun createLmuWindowsTyreTemperaturePreferencesRepository(
    directory: String,
): LmuWindowsTyreTemperaturePreferencesRepository =
    LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
        createLmuWindowsTyreTemperaturePreferencesDataStore(directory),
    )
