package kurou.kodriver.domain.manualreadout.usecase

import kurou.kodriver.domain.manualreadout.repository.ManualReadoutSettingsResetter

public class ResetManualReadoutSettingsUseCase(
    private val resetters: List<ManualReadoutSettingsResetter>,
) {
    public suspend operator fun invoke() {
        resetters.forEach { it.resetToDefault() }
    }
}
