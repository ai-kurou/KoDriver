package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository

class SaveOverlayTextColorUseCase(
    private val repository: OverlayTextColorPreferencesRepository,
) {
    suspend operator fun invoke(overlayTextColor: Long) = repository.saveOverlayTextColor(overlayTextColor)
}
