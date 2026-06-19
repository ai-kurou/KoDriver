package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerVersionRepository

class FetchServerVersionUseCase(
    private val repository: ServerVersionRepository,
) {
    suspend operator fun invoke(ip: String): Result<String> = repository.fetchVersion(ip)
}
