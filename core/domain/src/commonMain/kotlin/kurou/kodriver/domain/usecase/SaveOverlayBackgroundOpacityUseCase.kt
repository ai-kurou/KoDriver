package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_MAX
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_MIN
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

class SaveOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    suspend operator fun invoke(opacity: Int) {
        require(opacity in OVERLAY_BACKGROUND_OPACITY_MIN..OVERLAY_BACKGROUND_OPACITY_MAX) {
            "opacity must be between 0 and 100"
        }
        repository.saveOverlayBackgroundOpacity(opacity)
    }
}
