package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsBrakeTemperatureData

interface LmuWindowsBrakeTemperatureRepository {
    fun brakeTemperatureStream(): Flow<LmuWindowsBrakeTemperatureData>
}
