package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository

class CheckAceWindowsConnectionUseCase(
    private val repository: AceWindowsFuelRepository,
) {
    suspend operator fun invoke(): Boolean = repository.isConnected()
}
