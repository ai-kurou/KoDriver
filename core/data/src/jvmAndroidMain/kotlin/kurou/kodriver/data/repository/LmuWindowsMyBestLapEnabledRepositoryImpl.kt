package kurou.kodriver.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.repository.LmuWindowsMyBestLapEnabledRepository
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

internal class LmuWindowsMyBestLapEnabledRepositoryImpl(
    private val readoutPreferencesRepository: ReadoutPreferencesRepository,
) : LmuWindowsMyBestLapEnabledRepository {

    override fun observeEnabled(): Flow<Boolean> =
        readoutPreferencesRepository
            .observeReadoutEnabledStates(LMU_WINDOWS_SIMULATOR_ID)
            .map { it[ReadoutItemKey.MyBestLap] ?: false }

    override suspend fun saveEnabled(enabled: Boolean) {
        readoutPreferencesRepository.saveReadoutEnabledState(
            simulator = LMU_WINDOWS_SIMULATOR_ID,
            key = ReadoutItemKey.MyBestLap,
            enabled = enabled,
        )
    }

    private companion object {
        const val LMU_WINDOWS_SIMULATOR_ID = "lmu_windows"
    }
}
