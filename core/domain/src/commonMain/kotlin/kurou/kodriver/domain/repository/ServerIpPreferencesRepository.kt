package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

interface ServerIpPreferencesRepository {
    fun serverIp(): Flow<String?>
    suspend fun saveServerIp(ip: String)
}
