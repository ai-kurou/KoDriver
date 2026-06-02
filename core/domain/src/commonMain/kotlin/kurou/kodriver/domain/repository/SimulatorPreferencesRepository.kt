package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface SimulatorPreferencesRepository {
    fun selectedSimulator(): Flow<String?>
    suspend fun saveSelectedSimulator(simulator: String)
}
