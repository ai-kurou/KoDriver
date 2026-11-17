package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

public class PreviewOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    public operator fun invoke(opacity: Int?): Unit = repository.setPreviewOverlayBackgroundOpacity(opacity)
}
