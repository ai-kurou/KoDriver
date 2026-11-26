package kurou.kodriver.domain.serverconnection.usecase

import kurou.kodriver.domain.serverconnection.repository.ConsoleAddressPreferencesRepository

public class SaveConsoleAddressUseCase(
    private val repository: ConsoleAddressPreferencesRepository,
) {
    public suspend operator fun invoke(address: String): Unit = repository.saveConsoleAddress(address)
}
