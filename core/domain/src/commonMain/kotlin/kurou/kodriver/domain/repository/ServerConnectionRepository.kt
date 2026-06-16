package kurou.kodriver.domain.repository

interface ServerConnectionRepository {
    suspend fun isConnected(ip: String): Boolean
}
