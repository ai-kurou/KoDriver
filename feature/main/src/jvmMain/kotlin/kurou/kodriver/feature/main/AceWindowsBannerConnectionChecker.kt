package kurou.kodriver.feature.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kurou.kodriver.domain.usecase.CheckAceWindowsConnectionUseCase

/**
 * AceWindowsBanner の接続状態を監視する実装。
 */
class AceWindowsBannerConnectionChecker(
    private val checkAceWindowsConnection: CheckAceWindowsConnectionUseCase,
) : AceBannerConnectionChecker {
    override fun statusFlow(): Flow<ConnectionBannerVmStatus> =
        flow {
            while (true) {
                val isConnected =
                    try {
                        checkAceWindowsConnection()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        false
                    }
                emit(if (isConnected) ConnectionBannerVmStatus.CONNECTED else ConnectionBannerVmStatus.DISCONNECTED)
                delay(CONNECTION_CHECK_INTERVAL_MS)
            }
        }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
