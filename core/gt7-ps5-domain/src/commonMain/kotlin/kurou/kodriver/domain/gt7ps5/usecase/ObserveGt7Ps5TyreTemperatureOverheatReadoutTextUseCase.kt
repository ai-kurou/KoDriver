package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5TyreTemperaturePreferencesRepository

class ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
