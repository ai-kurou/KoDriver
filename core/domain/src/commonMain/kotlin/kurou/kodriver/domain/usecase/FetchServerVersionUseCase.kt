package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerVersionRepository

public class FetchServerVersionUseCase(
    private val repository: ServerVersionRepository,
) {
    public suspend operator fun invoke(ip: String): Result<String> = repository.fetchVersion(ip)
}
