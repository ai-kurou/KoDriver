package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.OverlayWindowBounds
import kurou.kodriver.domain.repository.OverlayWindowBoundsPreferencesRepository

public class SaveOverlayWindowBoundsUseCase(
    private val repository: OverlayWindowBoundsPreferencesRepository,
) {
    public suspend operator fun invoke(bounds: OverlayWindowBounds) {
        repository.saveOverlayWindowBounds(bounds)
    }
}
