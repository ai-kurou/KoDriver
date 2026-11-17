package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.repository.AudioInputDeviceRepository

public class GetAudioInputDevicesUseCase(
    private val repository: AudioInputDeviceRepository,
) {
    public suspend operator fun invoke(): List<AudioInputDevice> = repository.getDevices()
}
