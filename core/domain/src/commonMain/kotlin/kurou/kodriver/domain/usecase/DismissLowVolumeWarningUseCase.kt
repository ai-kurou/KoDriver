package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kurou.kodriver.domain.model.DismissedVolumes
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.repository.LowVolumeWarningDismissalRepository

class DismissLowVolumeWarningUseCase(
    private val repository: LowVolumeWarningDismissalRepository,
) {
    suspend operator fun invoke(warning: LowVolumeWarning) {
        val current = repository.observeDismissedVolumes().first()
        repository.saveDismissedVolumes(
            DismissedVolumes(
                deviceVolume = warning.deviceVolume ?: current.deviceVolume,
                soundVolume = warning.soundVolume ?: current.soundVolume,
            ),
        )
    }
}
