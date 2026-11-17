package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

public class PreviewOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    public operator fun invoke(argb: Long?): Unit = repository.setPreviewOverlayTextColor(argb)
}
