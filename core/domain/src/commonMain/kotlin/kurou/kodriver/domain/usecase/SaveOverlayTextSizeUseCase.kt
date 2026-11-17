package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository

public class SaveOverlayTextSizeUseCase(
    private val repository: OverlayTextSizePreferencesRepository,
) {
    public suspend operator fun invoke(overlayTextSize: OverlayTextSize): Unit =
        repository.saveOverlayTextSize(overlayTextSize)
}
