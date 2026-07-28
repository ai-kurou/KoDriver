package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow

interface AceBannerConnectionChecker {
    fun statusFlow(): Flow<ConnectionBannerVmStatus>
}
