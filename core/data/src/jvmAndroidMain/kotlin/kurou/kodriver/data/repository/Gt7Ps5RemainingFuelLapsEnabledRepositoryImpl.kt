package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class Gt7Ps5RemainingFuelLapsEnabledRepositoryImpl(
    private val readoutPreferencesRepository: ReadoutPreferencesRepository,
) : Gt7Ps5RemainingFuelLapsEnabledRepository {

    override fun observeEnabled(): Flow<Boolean?> =
        readoutPreferencesRepository
            .observeReadoutEnabledStates(Simulator.Gt7Ps5.id)
            .map { it[ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root] }

    override suspend fun saveEnabled(enabled: Boolean) {
        readoutPreferencesRepository.saveReadoutEnabledState(
            simulator = Simulator.Gt7Ps5.id,
            key = ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
            enabled = enabled,
        )
    }
}
