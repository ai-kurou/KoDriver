package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.TelemetryRepository

class CheckTelemetryConnectionUseCase(private val repository: TelemetryRepository) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
