package kurou.kodriver.core.devicevolumedata.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kurou.kodriver.core.devicevolumedata.windows.WasapiMasterVolumeSource
import kurou.kodriver.core.devicevolumedata.windows.WindowsMasterVolumeSource
import kurou.kodriver.domain.model.DEVICE_VOLUME_MAX
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kotlin.math.roundToInt

/**
 * Windows Core Audio（WASAPI）を介して既定のオーディオ出力デバイスのマスター音量（0-100）を
 * 取得・設定する。実際のCOM呼び出しは [WindowsMasterVolumeSource] に切り出しており、
 * ここでは0.0-1.0のスカラー値と0-100のパーセンテージ間の変換のみを担う。
 */
internal class WindowsDeviceVolumeRepository(
    private val masterVolumeSource: WindowsMasterVolumeSource = WasapiMasterVolumeSource(),
) : DeviceVolumeRepository {
    override suspend fun getVolume(): Int =
        withContext(Dispatchers.IO) {
            (masterVolumeSource.getScalarVolume() * DEVICE_VOLUME_MAX)
                .roundToInt()
                .coerceIn(DEVICE_VOLUME_MIN, DEVICE_VOLUME_MAX)
        }

    override suspend fun setVolume(volume: Int) {
        withContext(Dispatchers.IO) {
            masterVolumeSource.setScalarVolume(volume / DEVICE_VOLUME_MAX.toFloat())
        }
    }
}
