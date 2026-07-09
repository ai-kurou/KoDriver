package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class LmuWindowsMyBestLapEnabledRepositoryImpl(
    private val readoutPreferencesRepository: ReadoutPreferencesRepository,
) : LmuWindowsMyBestLapEnabledRepository {

    override fun observeEnabled(): Flow<Boolean?> =
        readoutPreferencesRepository
            .observeReadoutEnabledStates(Simulator.LmuWindows.id)
            .map { it[ReadoutItemKey.LmuWindows.MyBestLap.Root] }

    override suspend fun saveEnabled(enabled: Boolean) {
        readoutPreferencesRepository.saveReadoutEnabledState(
            simulator = Simulator.LmuWindows.id,
            key = ReadoutItemKey.LmuWindows.MyBestLap.Root,
            enabled = enabled,
        )
    }
}
