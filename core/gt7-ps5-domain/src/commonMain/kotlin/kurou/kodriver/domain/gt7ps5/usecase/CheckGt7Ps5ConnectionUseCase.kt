package kurou.kodriver.domain.gt7ps5.usecase

import kurou.kodriver.domain.gt7ps5.repository.Gt7Ps5Repository

public class CheckGt7Ps5ConnectionUseCase(
    private val repository: Gt7Ps5Repository,
) {
    public suspend operator fun invoke(): Boolean = repository.isConnected()
}
