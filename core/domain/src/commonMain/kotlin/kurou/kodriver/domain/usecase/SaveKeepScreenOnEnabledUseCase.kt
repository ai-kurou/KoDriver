package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository

public class SaveKeepScreenOnEnabledUseCase(
    private val repository: KeepScreenOnEnabledRepository,
) {
    public suspend operator fun invoke(enabled: Boolean): Unit = repository.saveKeepScreenOn(enabled)
}
