package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

class PreviewOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    operator fun invoke(argb: Long?) = repository.setPreviewOverlayTextColor(argb)
}
