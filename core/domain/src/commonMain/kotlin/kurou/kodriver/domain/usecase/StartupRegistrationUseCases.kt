package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.StartupRegistrationRepository

class StartupRegistrationUseCases(
    private val repository: StartupRegistrationRepository,
) {
    suspend fun getEnabled(): Boolean = repository.isEnabled()

    suspend fun setEnabled(enabled: Boolean) = repository.setEnabled(enabled)
}
