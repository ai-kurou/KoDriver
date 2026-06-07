package kurou.kodriver.feature.readout.vehicleapproach

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kurou.kodriver.domain.model.ProximityData
import kurou.kodriver.domain.repository.ProximityRepository

internal class FakeProximityRepository : ProximityRepository {
    private val flow = MutableSharedFlow<ProximityData>()

    override fun proximityStream(): Flow<ProximityData> = flow

    suspend fun emit(data: ProximityData) = flow.emit(data)
}
