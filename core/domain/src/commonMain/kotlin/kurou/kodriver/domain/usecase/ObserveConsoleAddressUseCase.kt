package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ConsoleAddressRepository

class ObserveConsoleAddressUseCase(private val repository: ConsoleAddressRepository) {
    operator fun invoke() = repository.consoleAddress()
}
