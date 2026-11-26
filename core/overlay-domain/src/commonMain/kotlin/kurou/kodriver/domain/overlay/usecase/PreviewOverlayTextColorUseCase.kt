package kurou.kodriver.domain.overlay.usecase

import kurou.kodriver.domain.overlay.repository.OverlayTextColorPreferencesRepository

public class PreviewOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    public operator fun invoke(argb: Long?): Unit = repository.setPreviewOverlayTextColor(argb)
}
