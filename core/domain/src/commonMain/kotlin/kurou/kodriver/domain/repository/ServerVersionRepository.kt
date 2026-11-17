package kurou.kodriver.domain.repository

public interface ServerVersionRepository {
    public suspend fun fetchVersion(ip: String): Result<String>
}
