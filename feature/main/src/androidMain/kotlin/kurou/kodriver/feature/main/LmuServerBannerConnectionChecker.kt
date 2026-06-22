package kurou.kodriver.feature.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

class LmuServerBannerConnectionChecker(
    private val fetchServerVersion: FetchServerVersionUseCase,
    private val observeServerIp: ObserveServerIpUseCase,
) : LmuBannerConnectionChecker {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun statusFlow(): Flow<ConnectionBannerVmStatus> = observeServerIp()
        .flatMapLatest { ip ->
            if (ip == null) {
                flowOf(ConnectionBannerVmStatus.IP_NOT_CONFIGURED)
            } else {
                flow {
                    emit(ConnectionBannerVmStatus.UNCHECKED)
                    while (true) {
                        val result = fetchServerVersion(ip)
                        emit(
                            if (result.isSuccess) ConnectionBannerVmStatus.CONNECTED
                            else ConnectionBannerVmStatus.DISCONNECTED,
                        )
                        delay(CONNECTION_CHECK_INTERVAL_MS)
                    }
                }
            }
        }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
