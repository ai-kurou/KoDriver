package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

public class SaveManualReadoutPlayButtonLayoutEnabledUseCase(
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository,
) {
    public suspend operator fun invoke(enabled: Boolean) {
        repository.saveManualReadoutPlayButtonLayoutEnabled(enabled)
    }
}
