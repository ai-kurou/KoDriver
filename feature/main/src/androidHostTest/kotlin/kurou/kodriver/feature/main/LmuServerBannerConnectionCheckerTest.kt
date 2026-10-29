package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

class LmuServerBannerConnectionCheckerTest : ServerBannerConnectionCheckerTestContract() {
    override fun createStatusFlow(
        fetchServerVersion: FetchServerVersionUseCase,
        observeServerIp: ObserveServerIpUseCase,
    ): Flow<ConnectionBannerVmStatus> =
        LmuServerBannerConnectionChecker(fetchServerVersion, observeServerIp).statusFlow()
}
