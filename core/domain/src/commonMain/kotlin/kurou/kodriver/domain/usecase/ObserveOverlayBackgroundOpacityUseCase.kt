package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.repository.OverlayBackgroundOpacityPreferencesRepository

class ObserveOverlayBackgroundOpacityUseCase(
    private val repository: OverlayBackgroundOpacityPreferencesRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observeOverlayBackgroundOpacity()
}
