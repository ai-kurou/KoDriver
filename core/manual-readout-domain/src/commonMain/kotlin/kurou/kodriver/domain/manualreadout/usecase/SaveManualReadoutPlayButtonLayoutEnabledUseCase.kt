package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository

public class SaveManualReadoutPlayButtonLayoutEnabledUseCase(
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository,
) {
    public suspend operator fun invoke(enabled: Boolean) {
        repository.saveManualReadoutPlayButtonLayoutEnabled(enabled)
    }
}
