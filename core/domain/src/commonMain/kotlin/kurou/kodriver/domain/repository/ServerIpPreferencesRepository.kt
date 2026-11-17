package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow

public interface ServerIpPreferencesRepository {
    public fun serverIp(): Flow<String?>

    public suspend fun saveServerIp(ip: String)
}
