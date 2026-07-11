package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository

class ObserveConsoleAddressUseCase(private val repository: ConsoleAddressPreferencesRepository) {
    operator fun invoke() = repository.consoleAddress()
}
