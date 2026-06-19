package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerVersionRepository

internal class FakeServerVersionRepository(
    private val result: Result<String> = Result.success("0.5.0"),
) : ServerVersionRepository {
    override suspend fun fetchVersion(ip: String): Result<String> = result
}
