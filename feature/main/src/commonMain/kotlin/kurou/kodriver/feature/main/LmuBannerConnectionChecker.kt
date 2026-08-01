package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow

/**
 * LmuBanner の接続状態を監視する実装。
 */
interface LmuBannerConnectionChecker {
    fun statusFlow(): Flow<ConnectionBannerVmStatus>
}
