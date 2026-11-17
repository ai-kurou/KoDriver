package kurou.kodriver.feature.main

import kotlinx.coroutines.flow.Flow

/**
 * AceBanner の接続状態を監視する実装。
 */
public interface AceBannerConnectionChecker {
    public fun statusFlow(): Flow<ConnectionBannerVmStatus>
}
