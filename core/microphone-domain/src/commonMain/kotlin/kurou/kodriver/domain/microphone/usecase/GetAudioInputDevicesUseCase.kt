package kurou.kodriver.domain.microphone.usecase

import kurou.kodriver.domain.microphone.model.AudioInputDevice
import kurou.kodriver.domain.microphone.repository.AudioInputDeviceRepository

public class GetAudioInputDevicesUseCase(
    private val repository: AudioInputDeviceRepository,
) {
    public suspend operator fun invoke(): List<AudioInputDevice> = repository.getDevices()
}
