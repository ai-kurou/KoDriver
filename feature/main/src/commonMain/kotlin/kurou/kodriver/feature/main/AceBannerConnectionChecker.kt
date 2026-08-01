package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow

/**
 * AceBanner の接続状態を監視する実装。
 */
interface AceBannerConnectionChecker {
    fun statusFlow(): Flow<ConnectionBannerVmStatus>
}
