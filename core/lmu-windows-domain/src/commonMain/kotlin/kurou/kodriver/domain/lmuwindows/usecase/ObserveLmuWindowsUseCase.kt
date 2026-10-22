package kurou.kodriver.domain.lmuwindows.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository

class ObserveLmuWindowsUseCase(
    private val repository: LmuWindowsRepository,
) {
    operator fun invoke(): Flow<LmuWindowsTelemetryData> = repository.telemetryStream()
}
