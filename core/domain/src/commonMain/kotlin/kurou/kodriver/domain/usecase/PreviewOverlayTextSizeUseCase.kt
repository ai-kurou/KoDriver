package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

class PreviewOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    operator fun invoke(overlayTextSize: OverlayTextSize?) = repository.setPreviewOverlayTextSize(overlayTextSize)
}
