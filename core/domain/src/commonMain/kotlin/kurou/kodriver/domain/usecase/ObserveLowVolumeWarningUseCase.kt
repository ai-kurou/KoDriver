package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kurou.kodriver.domain.model.DEVICE_VOLUME_WARNING_THRESHOLD_DEFAULT
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.model.SOUND_VOLUME_WARNING_THRESHOLD_DEFAULT

class ObserveLowVolumeWarningUseCase(
    private val observeDeviceVolume: ObserveDeviceVolumeUseCase,
    private val observeSoundVolume: ObserveSoundVolumeUseCase,
) {
    operator fun invoke(): Flow<LowVolumeWarning?> =
        combine(observeDeviceVolume(), observeSoundVolume()) { deviceVolume, soundVolume ->
            val warningDeviceVolume = deviceVolume.takeIf { it < DEVICE_VOLUME_WARNING_THRESHOLD_DEFAULT }
            val warningSoundVolume = soundVolume.takeIf { it < SOUND_VOLUME_WARNING_THRESHOLD_DEFAULT }
            if (warningDeviceVolume == null && warningSoundVolume == null) {
                null
            } else {
                LowVolumeWarning(warningDeviceVolume, warningSoundVolume)
            }
        }
}
