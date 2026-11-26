package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository

public class SaveOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    public suspend operator fun invoke(overlayTextColor: Long): Unit = repository.saveOverlayTextColor(overlayTextColor)
}
