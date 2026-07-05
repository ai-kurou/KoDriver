package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.TyreCarcassTemperatureData

interface TyreCarcassTemperatureRepository {
    fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData>
}
