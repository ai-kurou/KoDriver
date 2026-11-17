package kurou.kodriver.domain.lmuwindows.usecase

import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsRepository

public class CheckLmuWindowsConnectionUseCase(
    private val repository: LmuWindowsRepository,
) {
    public suspend operator fun invoke(): Boolean = repository.isConnected()
}
