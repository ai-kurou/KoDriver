package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.model.OVERLAY_BACKGROUND_OPACITY_MAX
import kurou.kodriver.domain.overlay.model.OVERLAY_BACKGROUND_OPACITY_MIN
import kurou.kodriver.domain.overlay.repository.OverlayBackgroundOpacityPreferencesRepository

public class SaveOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    public suspend operator fun invoke(opacity: Int) {
        require(opacity in OVERLAY_BACKGROUND_OPACITY_MIN..OVERLAY_BACKGROUND_OPACITY_MAX) {
            "opacity must be between 0 and 100"
        }
        repository.saveOverlayBackgroundOpacity(opacity)
    }
}
