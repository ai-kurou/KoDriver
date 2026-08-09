package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.core.model.LmuWindowsTyreCarcassTemperatureData

interface LmuWindowsTyreCarcassTemperatureRepository {
    fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData>
}
