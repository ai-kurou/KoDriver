package kurou.kodriver.data.microphone

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.model.AudioInputDevice
import kurou.kodriver.domain.repository.AudioInputDeviceRepository

/**
 * [AudioInputSource] が返す入力デバイスを一覧にする [AudioInputDeviceRepository]。
 * デバイスの列挙はドライバを呼ぶため [ioDispatcher] 上で行う。
 */
internal class JvmAudioInputDeviceRepository(
    private val source: AudioInputSource = JavaSoundAudioInputSource(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AudioInputDeviceRepository {
    override suspend fun getDevices(): List<AudioInputDevice> =
        withContext(ioDispatcher) { source.deviceNames().map(::AudioInputDevice) }
}
