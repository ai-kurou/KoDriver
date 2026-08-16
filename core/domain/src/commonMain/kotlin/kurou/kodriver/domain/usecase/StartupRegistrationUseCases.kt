package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.StartupEnabledRepository

class StartupRegistrationUseCases(
    private val repository: StartupEnabledRepository,
) {
    suspend fun getEnabled(): Boolean = repository.isEnabled()

    suspend fun setEnabled(enabled: Boolean) = repository.setEnabled(enabled)
}
