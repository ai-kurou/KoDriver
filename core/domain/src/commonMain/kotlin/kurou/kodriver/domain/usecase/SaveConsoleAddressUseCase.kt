package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ConsoleAddressRepository

class SaveConsoleAddressUseCase(private val repository: ConsoleAddressRepository) {
    suspend operator fun invoke(address: String) = repository.saveConsoleAddress(address)
}
