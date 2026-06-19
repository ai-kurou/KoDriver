package kurou.kodriver.domain.repository

interface ServerVersionRepository {
    suspend fun fetchVersion(ip: String): Result<String>
}
