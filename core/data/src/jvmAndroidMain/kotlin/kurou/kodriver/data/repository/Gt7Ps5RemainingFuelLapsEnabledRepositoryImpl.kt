package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl(
    private val readoutPreferencesRepository: ReadoutPreferencesRepository,
) : Gt7Ps5RemainingFuelLapsEnabledRepository {

    override fun observeEnabled(): Flow<Boolean> =
        readoutPreferencesRepository
            .observeReadoutEnabledStates(GT7_PS5_SIMULATOR_ID)
            .map { it[ReadoutItemKey.RemainingFuelLaps] ?: true }

    override suspend fun saveEnabled(enabled: Boolean) {
        readoutPreferencesRepository.saveReadoutEnabledState(
            simulator = GT7_PS5_SIMULATOR_ID,
            key = ReadoutItemKey.RemainingFuelLaps,
            enabled = enabled,
        )
    }

    private companion object {
        const val GT7_PS5_SIMULATOR_ID = "gt7_ps5"
    }
}
