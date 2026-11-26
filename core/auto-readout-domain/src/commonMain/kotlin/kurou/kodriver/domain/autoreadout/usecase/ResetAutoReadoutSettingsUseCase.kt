package kurou.kodriver.domain.autoreadout.usecase

import kurou.kodriver.domain.autoreadout.repository.AutoReadoutSettingsResetter

public class ResetAutoReadoutSettingsUseCase(
    private val resetters: List<AutoReadoutSettingsResetter>,
) {
    public suspend operator fun invoke() {
        resetters.forEach { it.resetToDefault() }
    }
}
