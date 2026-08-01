package kurou.kodriver.data

import kurou.kodriver.data.datasource.createLmuWindowsTyreTemperaturePreferencesDataStore
import kurou.kodriver.data.repository.LmuWindowsTyreTemperaturePreferencesRepositoryImpl
import kurou.kodriver.domain.repository.LmuWindowsTyreTemperaturePreferencesRepository

/**
 * LmuWindowsTyreTemperaturePreferences Repository の永続化実装を生成する。
 */
fun createLmuWindowsTyreTemperaturePreferencesRepository(directory: String): LmuWindowsTyreTemperaturePreferencesRepository =
    LmuWindowsTyreTemperaturePreferencesRepositoryImpl(
        createLmuWindowsTyreTemperaturePreferencesDataStore(directory),
    )
