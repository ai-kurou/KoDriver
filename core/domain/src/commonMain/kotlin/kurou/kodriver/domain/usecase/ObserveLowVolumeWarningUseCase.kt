package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import kurou.kodriver.domain.model.DEVICE_VOLUME_WARNING_THRESHOLD_DEFAULT
import kurou.kodriver.domain.model.DismissedVolumes
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.model.SOUND_VOLUME_WARNING_THRESHOLD_DEFAULT
import kurou.kodriver.domain.repository.LowVolumeWarningDismissalRepository

class ObserveLowVolumeWarningUseCase(
    private val observeDeviceVolume: ObserveDeviceVolumeUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
    private val dismissalRepository: LowVolumeWarningDismissalRepository,
) {
    operator fun invoke(): Flow<LowVolumeWarning?> =
        combine(
            observeDeviceVolume(),
            observeSoundVolume(),
            dismissalRepository.observeDismissedVolumes(),
        ) { deviceVolume, soundVolume, dismissedVolumes ->
            WarningState(deviceVolume, soundVolume, dismissedVolumes)
        }.transform { state ->
            val cleared =
                state.dismissedVolumes.clearedAtRecoveredVolumes(
                    state.deviceVolume,
                    state.soundVolume,
                )
            if (cleared != state.dismissedVolumes) dismissalRepository.saveDismissedVolumes(cleared)
            emit(determineWarning(state.deviceVolume, state.soundVolume, cleared))
        }

    private data class WarningState(
        val deviceVolume: Int,
        val soundVolume: Int,
        val dismissedVolumes: DismissedVolumes,
    )
}

internal fun determineWarning(
    deviceVolume: Int,
    soundVolume: Int,
    dismissedVolumes: DismissedVolumes,
): LowVolumeWarning? {
    val warningDeviceVolume =
        deviceVolume.takeIf {
            it < DEVICE_VOLUME_WARNING_THRESHOLD_DEFAULT &&
                (dismissedVolumes.deviceVolume == null || it < dismissedVolumes.deviceVolume)
        }
    val warningSoundVolume =
        soundVolume.takeIf {
            it < SOUND_VOLUME_WARNING_THRESHOLD_DEFAULT &&
                (dismissedVolumes.soundVolume == null || it < dismissedVolumes.soundVolume)
        }
    if (warningDeviceVolume == null && warningSoundVolume == null) return null
    return LowVolumeWarning(warningDeviceVolume, warningSoundVolume)
}

internal fun DismissedVolumes.clearedAtRecoveredVolumes(
    deviceVolume: Int,
    soundVolume: Int,
): DismissedVolumes =
    DismissedVolumes(
        deviceVolume = if (deviceVolume >= DEVICE_VOLUME_WARNING_THRESHOLD_DEFAULT) null else this.deviceVolume,
        soundVolume = if (soundVolume >= SOUND_VOLUME_WARNING_THRESHOLD_DEFAULT) null else this.soundVolume,
    )
