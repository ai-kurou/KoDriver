package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

public class SaveConsoleAddressUseCase(
    private val repository: ConsoleAddressPreferencesRepository,
) {
    public suspend operator fun invoke(address: String): Unit = repository.saveConsoleAddress(address)
}
