package kurou.kodriver.domain.serverconnection.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.serverconnection.repository.ConsoleAddressPreferencesRepository

public class ObserveConsoleAddressUseCase(
    private val repository: ConsoleAddressPreferencesRepository,
) {
    public operator fun invoke(): Flow<String?> = repository.consoleAddress()
}
