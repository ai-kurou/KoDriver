package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository

public class PreviewOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    public operator fun invoke(opacity: Int?): Unit = repository.setPreviewOverlayBackgroundOpacity(opacity)
}
