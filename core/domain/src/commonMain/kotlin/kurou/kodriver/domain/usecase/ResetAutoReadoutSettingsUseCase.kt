package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter

class ResetAutoReadoutSettingsUseCase(
    private val resetters: List<AutoReadoutSettingsResetter>,
) {
    suspend operator fun invoke() {
        resetters.forEach { it.resetToDefault() }
    }
}
