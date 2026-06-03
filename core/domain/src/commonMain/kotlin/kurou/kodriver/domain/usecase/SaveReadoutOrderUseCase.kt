package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ReadoutPreferencesRepository

class SaveReadoutOrderUseCase(private val repository: ReadoutPreferencesRepository) {
    suspend operator fun invoke(simulator: String, order: List<String>) =
        repository.saveReadoutOrder(simulator, order)
}
