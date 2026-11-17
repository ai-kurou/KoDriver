package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachPreferencesRepository
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleApproachReadoutTextPreferencesRepository

public class LmuWindowsVehicleApproachPreferencesUseCases(
    private val repository: LmuWindowsVehicleApproachPreferencesRepository,
    private val readoutTextRepository: LmuWindowsVehicleApproachReadoutTextPreferencesRepository,
) {
    public fun observeSkipFirstLap(): Flow<Boolean> = repository.observeSkipFirstLap()

    public suspend fun saveSkipFirstLap(skip: Boolean): Unit = repository.saveSkipFirstLap(skip)

    public fun observeStartLeftReadoutText(): Flow<String> = readoutTextRepository.observeStartLeftReadoutText()

    public suspend fun saveStartLeftReadoutText(text: String): Unit =
        SaveLmuWindowsVehicleApproachStartLeftReadoutTextUseCase(readoutTextRepository)(text)

    public fun observeStartRightReadoutText(): Flow<String> = readoutTextRepository.observeStartRightReadoutText()

    public suspend fun saveStartRightReadoutText(text: String): Unit =
        SaveLmuWindowsVehicleApproachStartRightReadoutTextUseCase(readoutTextRepository)(text)

    public fun observeSustainedLeftReadoutText(): Flow<String> = readoutTextRepository.observeSustainedLeftReadoutText()

    public suspend fun saveSustainedLeftReadoutText(text: String): Unit =
        SaveLmuWindowsVehicleApproachSustainedLeftReadoutTextUseCase(readoutTextRepository)(text)

    public fun observeSustainedRightReadoutText(): Flow<String> =
        readoutTextRepository.observeSustainedRightReadoutText()

    public suspend fun saveSustainedRightReadoutText(text: String): Unit =
        SaveLmuWindowsVehicleApproachSustainedRightReadoutTextUseCase(readoutTextRepository)(text)
}
