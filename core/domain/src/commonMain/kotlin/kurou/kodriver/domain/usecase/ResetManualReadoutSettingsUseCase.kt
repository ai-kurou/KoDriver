package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutSettingsResetter

public class ResetManualReadoutSettingsUseCase(
    private val resetters: List<ManualReadoutSettingsResetter>,
) {
    public suspend operator fun invoke() {
        resetters.forEach { it.resetToDefault() }
    }
}
