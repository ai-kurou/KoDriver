package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.ManualReadoutSettingsResetter

class ResetManualReadoutSettingsUseCase(
    private val resetters: List<ManualReadoutSettingsResetter>,
) {
    suspend operator fun invoke() {
        resetters.forEach { it.resetToDefault() }
    }
}
