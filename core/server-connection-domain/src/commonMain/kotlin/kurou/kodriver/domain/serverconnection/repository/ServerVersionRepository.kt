package kurou.kodriver.domain.serverconnection.repository

public interface ServerVersionRepository {
    public suspend fun fetchVersion(ip: String): Result<String>
}
