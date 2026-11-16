package kurou.kodriver.domain.acewindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.acewindows.repository.AceWindowsVehicleApproachPreferencesRepository

public class AceWindowsVehicleApproachThresholdsUseCases(
    private val repository: AceWindowsVehicleApproachPreferencesRepository,
) {
    public fun observeThresholdMeters(): Flow<Double> = repository.observeThresholdMeters()

    public suspend fun saveThresholdMeters(meters: Double): Unit = repository.saveThresholdMeters(meters)
}
