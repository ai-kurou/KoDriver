package kurou.kodriver.core.devicevolumedata

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

internal const val DEVICE_VOLUME_POLLING_INTERVAL_MILLIS = 1_000L

internal fun pollingVolumeFlow(
    intervalMillis: Long = DEVICE_VOLUME_POLLING_INTERVAL_MILLIS,
    getVolume: suspend () -> Int,
): Flow<Int> =
    flow {
        while (true) {
            emit(getVolume())
            delay(intervalMillis)
        }
    }.distinctUntilChanged()
