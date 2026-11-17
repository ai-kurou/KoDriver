package kurou.kodriver.domain.gt7ps5.repository

import kotlinx.coroutines.flow.Flow

public interface Gt7Ps5UdpPortPreferencesRepository {
    public fun port(): Flow<Int>

    public suspend fun savePort(port: Int)
}
