package kurou.kodriver.domain.lmuwindows.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTyreCarcassTemperatureData

public interface LmuWindowsTyreCarcassTemperatureRepository {
    public fun tyreCarcassTemperatureStream(): Flow<LmuWindowsTyreCarcassTemperatureData>
}
