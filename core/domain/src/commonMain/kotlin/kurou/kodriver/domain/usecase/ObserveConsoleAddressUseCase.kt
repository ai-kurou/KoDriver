package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

public class ObserveConsoleAddressUseCase(
    private val repository: ConsoleAddressPreferencesRepository,
) {
    public operator fun invoke(): Flow<String?> = repository.consoleAddress()
}
