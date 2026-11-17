package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

public class SaveServerIpUseCase(
    private val repository: ServerIpPreferencesRepository,
) {
    public suspend operator fun invoke(ip: String): Unit = repository.saveServerIp(ip)
}
