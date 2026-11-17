package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData

public interface LmuWindowsBrakeTemperatureRepository {
    public fun brakeTemperatureStream(): Flow<LmuWindowsBrakeTemperatureData>
}
