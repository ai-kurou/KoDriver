package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.model.OverlayTextSize
import kurou.kodriver.domain.overlay.repository.OverlayTextSizePreferencesRepository

public class PreviewOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    public operator fun invoke(overlayTextSize: OverlayTextSize?): Unit =
        repository.setPreviewOverlayTextSize(overlayTextSize)
}
