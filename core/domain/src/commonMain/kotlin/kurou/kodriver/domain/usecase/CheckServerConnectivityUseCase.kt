package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerConnectivityRepository

public class CheckServerConnectivityUseCase(
    private val repository: ServerConnectivityRepository,
) {
    public suspend operator fun invoke(ip: String): Boolean = repository.isReachable(ip)
}
