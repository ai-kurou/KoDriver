package kurou.kodriver.domain.gt7ps5.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5RemainingFuelLapsPreferencesRepository

public class ObserveGt7Ps5RemainingFuelLapsEmptyReadoutTextUseCase(
    private val repository: Gt7Ps5RemainingFuelLapsPreferencesRepository,
) {
    public operator fun invoke(): Flow<String> = repository.observeEmptyReadoutText()
}
