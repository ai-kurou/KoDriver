package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository

public class SaveOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    public suspend operator fun invoke(overlayTextSize: OverlayTextSize): Unit =
        repository.saveOverlayTextSize(overlayTextSize)
}
