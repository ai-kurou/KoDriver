package kurou.kodriver.domain.repository

import kotlinx.coroutines.flow.Flow
import kurou.kodriver.domain.model.DiscoveredServer

/** LAN 内の Windows 版 KoDriver を検出する。購読している間だけ検出し、見つかったサーバーの一覧を配信する。 */
public fun interface WindowsServerDiscoveryRepository {
    public fun discover(): Flow<List<DiscoveredServer>>
}
