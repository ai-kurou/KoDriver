package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository

class SaveGt7Ps5UdpPortUseCase(private val repository: Gt7Ps5UdpPortPreferencesRepository) {
    suspend operator fun invoke(port: Int) {
        require(port == 33740 || port == 33741) { "port must be 33740 or 33741" }
        repository.savePort(port)
    }
}
