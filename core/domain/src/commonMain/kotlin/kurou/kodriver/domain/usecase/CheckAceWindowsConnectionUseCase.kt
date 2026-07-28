package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AceWindowsFuelRepository

class CheckAceWindowsConnectionUseCase(
    private val repository: AceWindowsFuelRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
