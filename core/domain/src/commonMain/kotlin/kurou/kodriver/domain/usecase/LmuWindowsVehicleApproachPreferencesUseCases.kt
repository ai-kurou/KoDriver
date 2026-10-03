package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

class LmuWindowsVehicleApproachPreferencesUseCases(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
    private val readoutTextRepository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    fun observeSkipFirstLap(): Flow<Boolean> = repository.observeSkipFirstLap()

    suspend fun saveSkipFirstLap(skip: Boolean) = repository.saveSkipFirstLap(skip)

    fun observeStartLeftReadoutText(): Flow<String> = readoutTextRepository.observeStartLeftReadoutText()

    suspend fun saveStartLeftReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(readoutTextRepository)(text)

    fun observeStartRightReadoutText(): Flow<String> = readoutTextRepository.observeStartRightReadoutText()

    suspend fun saveStartRightReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(readoutTextRepository)(text)

    fun observeSustainedLeftReadoutText(): Flow<String> = readoutTextRepository.observeSustainedLeftReadoutText()

    suspend fun saveSustainedLeftReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(readoutTextRepository)(text)

    fun observeSustainedRightReadoutText(): Flow<String> = readoutTextRepository.observeSustainedRightReadoutText()

    suspend fun saveSustainedRightReadoutText(text: String) =
        SaveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(readoutTextRepository)(text)
}
