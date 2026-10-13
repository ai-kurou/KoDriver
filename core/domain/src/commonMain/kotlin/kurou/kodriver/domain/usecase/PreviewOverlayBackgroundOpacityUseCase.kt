package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

class PreviewOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    operator fun invoke(opacity: Int?) = repository.setPreviewOverlayBackgroundOpacity(opacity)
}
