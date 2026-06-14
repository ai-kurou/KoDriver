package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerIpRepository

class SaveServerIpUseCase(private val repository: ServerIpRepository) {
    suspend operator fun invoke(ip: String) = repository.saveServerIp(ip)
}
