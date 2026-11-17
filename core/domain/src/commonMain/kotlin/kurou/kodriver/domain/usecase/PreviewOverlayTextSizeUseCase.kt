package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

public class PreviewOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    public operator fun invoke(overlayTextSize: OverlayTextSize?): Unit =
        repository.setPreviewOverlayTextSize(overlayTextSize)
}
