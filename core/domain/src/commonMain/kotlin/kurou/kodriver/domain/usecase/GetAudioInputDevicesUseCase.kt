package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.repository.AudioInputDeviceRepository

class GetAudioInputDevicesUseCase(
    private val repository: AudioInputDeviceRepository,
) {
    suspend operator fun invoke(): List<AudioInputDevice> = repository.getDevices()
}
