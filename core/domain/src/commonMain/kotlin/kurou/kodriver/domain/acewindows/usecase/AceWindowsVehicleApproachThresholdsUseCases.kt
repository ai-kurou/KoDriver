package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository

class AceWindowsVehicleApproachThresholdsUseCases(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    fun observeThresholdMeters(): Flow<Double> = repository.observeThresholdMeters()

    suspend fun saveThresholdMeters(meters: Double) = repository.saveThresholdMeters(meters)
}
