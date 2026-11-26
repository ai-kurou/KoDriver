package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

public class SaveOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    public suspend operator fun invoke(overlayTextColor: Long): Unit = repository.saveOverlayTextColor(overlayTextColor)
}
