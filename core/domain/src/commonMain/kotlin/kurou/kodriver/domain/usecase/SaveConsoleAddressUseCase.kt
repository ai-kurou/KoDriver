package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

class SaveConsoleAddressUseCase(
    private val repository: ConsoleAddressPreferencesRepository,
) {
    suspend operator fun invoke(address: String) = repository.saveConsoleAddress(address)
}
