package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository

class SaveGt7UdpPortUseCase(private val repository: Gt7UdpPortPreferencesRepository) {
    suspend operator fun invoke(port: Int) {
        require(port == 33740 || port == 33741) { "port must be 33740 or 33741" }
        repository.savePort(port)
    }
}
