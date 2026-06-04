package kurou.kodriver.data

import kurou.kodriver.data.datasource.createSimulatorPreferencesDataStore
import kurou.kodriver.data.repository.SimulatorPreferencesRepositoryImpl
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository

internal fun createSimulatorPreferencesRepository(directory: String): SimulatorPreferencesRepository =
    SimulatorPreferencesRepositoryImpl(createSimulatorPreferencesDataStore(directory))
