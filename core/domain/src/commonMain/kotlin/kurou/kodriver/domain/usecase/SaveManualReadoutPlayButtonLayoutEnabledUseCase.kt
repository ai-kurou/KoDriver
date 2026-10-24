package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

class SaveManualReadoutPlayButtonLayoutEnabledUseCase(
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        repository.saveManualReadoutPlayButtonLayoutEnabled(enabled)
    }
}
