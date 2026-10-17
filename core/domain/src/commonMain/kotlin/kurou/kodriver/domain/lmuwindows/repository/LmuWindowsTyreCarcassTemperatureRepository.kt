package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData

interface LmuWindowsTyreCarcassTemperatureRepository {
    fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData>
}
