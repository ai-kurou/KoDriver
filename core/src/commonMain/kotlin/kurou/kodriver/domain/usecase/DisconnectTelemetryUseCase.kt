package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TelemetryRepository

class DisconnectTelemetryUseCase(private val repository: TelemetryRepository) {
    suspend operator fun invoke() = repository.disconnect()
}
