package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow

interface LmuBannerConnectionChecker {
    fun statusFlow(): Flow<ConnectionBannerVmStatus>
}
