package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.Gt7Ps5TyreTemperaturePreferencesRepository

class ObserveGt7Ps5TyreTemperatureOverheatReadoutTextUseCase(
    private val repository: Gt7Ps5TyreTemperaturePreferencesRepository,
) {
    operator fun invoke(): Flow<String> = repository.observeOverheatReadoutText()
}
