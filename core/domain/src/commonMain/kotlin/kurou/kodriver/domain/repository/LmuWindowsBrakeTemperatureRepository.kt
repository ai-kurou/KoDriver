package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsBrakeTemperatureData

interface LmuWindowsBrakeTemperatureRepository {
    fun brakeTemperatureStream(): Flow<LmuWindowsBrakeTemperatureData>
}
