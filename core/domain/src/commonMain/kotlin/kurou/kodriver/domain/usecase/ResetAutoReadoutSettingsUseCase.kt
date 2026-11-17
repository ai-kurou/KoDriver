package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.AutoReadoutSettingsResetter

public class ResetAutoReadoutSettingsUseCase(
    private val resetters: List<AutoReadoutSettingsResetter>,
) {
    public suspend operator fun invoke() {
        resetters.forEach { it.resetToDefault() }
    }
}
