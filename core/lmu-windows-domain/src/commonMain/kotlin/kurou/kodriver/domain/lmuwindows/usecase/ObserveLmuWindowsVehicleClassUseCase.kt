package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsVehicleClassRepository

public class ObserveLmuWindowsVehicleClassUseCase(
    private val repository: LmuWindowsVehicleClassRepository,
) {
    public operator fun invoke(): Flow<LmuWindowsVehicleClassData> = repository.vehicleClassStream()
}
