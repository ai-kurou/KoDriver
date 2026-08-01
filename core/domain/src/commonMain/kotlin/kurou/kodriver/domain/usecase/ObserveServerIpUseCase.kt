package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

class ObserveServerIpUseCase(
    private val repository: ServerIpPreferencesRepository,
) {
    operator fun invoke() = repository.serverIp()
}
