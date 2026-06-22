package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.Gt7Ps5AddressRepository

class ObserveGt7Ps5AddressUseCase(private val repository: Gt7Ps5AddressRepository) {
    operator fun invoke() = repository.gt7Ps5Address()
}
