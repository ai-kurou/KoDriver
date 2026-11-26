package kurou.kodriver.domain.serverconnection.usecase

import kurou.kodriver.domain.serverconnection.repository.ServerIpPreferencesRepository

public class SaveServerIpUseCase(
    private val repository: ServerIpPreferencesRepository,
) {
    public suspend operator fun invoke(ip: String): Unit = repository.saveServerIp(ip)
}
