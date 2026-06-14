package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerIpRepository

class ObserveServerIpUseCase(private val repository: ServerIpRepository) {
    operator fun invoke() = repository.serverIp()
}
