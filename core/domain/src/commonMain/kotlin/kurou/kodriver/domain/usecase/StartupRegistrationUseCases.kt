package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.StartupEnabledRepository

public class StartupRegistrationUseCases(
    private val repository: StartupEnabledRepository,
) {
    public suspend fun getEnabled(): Boolean = repository.isEnabled()

    public suspend fun setEnabled(enabled: Boolean): Unit = repository.setEnabled(enabled)
}
