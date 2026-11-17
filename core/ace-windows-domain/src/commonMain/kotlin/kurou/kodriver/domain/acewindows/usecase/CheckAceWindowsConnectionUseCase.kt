package kurou.kodriver.domain.acewindows.usecase

import kurou.kodriver.domain.acewindows.repository.AceWindowsFuelRepository

public class CheckAceWindowsConnectionUseCase(
    private val repository: AceWindowsFuelRepository,
) {
    public suspend operator fun invoke(): Boolean = repository.isConnected()
}
