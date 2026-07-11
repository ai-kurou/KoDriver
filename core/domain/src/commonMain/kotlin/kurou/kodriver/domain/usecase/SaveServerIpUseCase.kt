package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ServerIpPreferencesRepository

class SaveServerIpUseCase(private val repository: ServerIpPreferencesRepository) {
    suspend operator fun invoke(ip: String) = repository.saveServerIp(ip)
}
