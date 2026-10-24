package kurou.kodriver.core.devicevolumedata

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

internal const val DEVICE_VOLUME_POLLING_INTERVAL_MILLIS = 1_000L

/**
 * 一定間隔で音量を取得して流す。既定の出力デバイスがない場合などで取得に失敗しても監視を止めず、
 * その回は値を流さず次回の取得を続ける（初回から失敗し続ける間は何も流れない）。
 */
internal fun pollingVolumeFlow(
    intervalMillis: Long = DEVICE_VOLUME_POLLING_INTERVAL_MILLIS,
    getVolume: suspend () -> Int,
): Flow<Int> =
    flow {
        while (true) {
            val volume =
                try {
                    getVolume()
                } catch (e: CancellationException) {
                    throw e
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    null
                }
            if (volume != null) emit(volume)
            delay(intervalMillis)
        }
    }.distinctUntilChanged()
