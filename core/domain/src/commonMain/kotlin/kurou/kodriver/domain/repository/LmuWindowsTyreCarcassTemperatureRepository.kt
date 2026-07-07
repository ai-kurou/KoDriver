package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.LmuWindowsTyreCarcassTemperatureData

interface LmuWindowsTyreCarcassTemperatureRepository {
    fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData>
}
