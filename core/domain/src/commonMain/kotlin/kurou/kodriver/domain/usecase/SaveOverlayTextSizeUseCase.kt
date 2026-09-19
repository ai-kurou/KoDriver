package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

class SaveOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    suspend operator fun invoke(overlayTextSize: OverlayTextSize) = repository.saveOverlayTextSize(overlayTextSize)
}
