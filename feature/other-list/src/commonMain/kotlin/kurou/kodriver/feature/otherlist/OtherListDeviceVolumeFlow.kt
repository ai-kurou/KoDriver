package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.model.OTHER_LIST_DEVICE_VOLUME_POLLING_INTERVAL_MS
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase

/** OtherList が購読中に取得する端末マスター音量を提供する。 */
class OtherListDeviceVolumeFlow(
    private val getDeviceVolume: GetDeviceVolumeUseCase,
) {
    val volume: Flow<Int> =
        flow {
            var latest = DEVICE_VOLUME_MIN
            while (true) {
                latest =
                    try {
                        getDeviceVolume()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (
                        @Suppress("TooGenericExceptionCaught") e: Exception,
                    ) {
                        latest
                    }
                emit(latest)
                delay(OTHER_LIST_DEVICE_VOLUME_POLLING_INTERVAL_MS)
            }
        }
}
