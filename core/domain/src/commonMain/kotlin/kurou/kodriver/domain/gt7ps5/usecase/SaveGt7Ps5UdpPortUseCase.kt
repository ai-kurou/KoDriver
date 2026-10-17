package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.model.gt7Ps5UdpPorts
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5UdpPortPreferencesRepository

class SaveGt7Ps5UdpPortUseCase(
    private val repository: Gt7Ps5UdpPortPreferencesRepository,
) {
    suspend operator fun invoke(port: Int) {
        require(port in gt7Ps5UdpPorts) { "port must be 33740 or 33741" }
        repository.savePort(port)
    }
}
