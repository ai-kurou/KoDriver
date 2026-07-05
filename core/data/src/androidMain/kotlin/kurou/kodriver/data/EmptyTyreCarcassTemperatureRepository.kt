package kurou.kodriver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kurou.kodriver.domain.model.TyreCarcassTemperatureData
import kurou.kodriver.domain.repository.TyreCarcassTemperatureRepository

internal class EmptyTyreCarcassTemperatureRepository : TyreCarcassTemperatureRepository {
    override fun tyreCarcassTemperatureStream(): Flow<TyreCarcassTemperatureData> = emptyFlow()
}
