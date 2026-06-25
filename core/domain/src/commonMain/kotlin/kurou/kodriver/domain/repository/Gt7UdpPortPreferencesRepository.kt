package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface Gt7UdpPortPreferencesRepository {
    fun port(): Flow<Int>
    suspend fun savePort(port: Int)
}
