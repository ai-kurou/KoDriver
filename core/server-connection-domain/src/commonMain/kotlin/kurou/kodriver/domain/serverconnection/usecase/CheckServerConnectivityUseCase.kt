package kurou.kodriver.domain.serverconnection.usecase

import kurou.kodriver.domain.serverconnection.repository.ServerConnectivityRepository

public class CheckServerConnectivityUseCase(
    private val repository: ServerConnectivityRepository,
) {
    public suspend operator fun invoke(ip: String): Boolean = repository.isReachable(ip)
}
