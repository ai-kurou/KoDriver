package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.AceWindowsFuelData

interface AceWindowsFuelRepository {
    fun fuelStream(): Flow<AceWindowsFuelData>
    suspend fun isConnected(): Boolean
}
