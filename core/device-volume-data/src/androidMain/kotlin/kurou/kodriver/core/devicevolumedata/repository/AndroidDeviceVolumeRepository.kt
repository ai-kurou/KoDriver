package kurou.kodriver.core.devicevolumedata.repository

import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.domain.model.DEVICE_VOLUME_MAX
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kotlin.math.roundToInt

/**
 * [AudioManager]（`STREAM_MUSIC`）を介して端末のマスター音量（0-100）を取得・設定する。
 */
internal class AndroidDeviceVolumeRepository(
    private val audioManager: AudioManager,
) : DeviceVolumeRepository {
    override suspend fun getVolume(): Int =
        withContext(Dispatchers.IO) {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (max <= 0) {
                DEVICE_VOLUME_MIN
            } else {
                val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                (current.toFloat() / max * DEVICE_VOLUME_MAX)
                    .roundToInt()
                    .coerceIn(DEVICE_VOLUME_MIN, DEVICE_VOLUME_MAX)
            }
        }

    override suspend fun setVolume(volume: Int) {
        withContext(Dispatchers.IO) {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (volume / DEVICE_VOLUME_MAX.toFloat() * max).roundToInt().coerceIn(0, max)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }
}
