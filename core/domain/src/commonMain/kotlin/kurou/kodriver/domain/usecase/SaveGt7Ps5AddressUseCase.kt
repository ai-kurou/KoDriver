package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7Ps5AddressRepository

class SaveGt7Ps5AddressUseCase(private val repository: Gt7Ps5AddressRepository) {
    suspend operator fun invoke(address: String) = repository.saveGt7Ps5Address(address)
}
