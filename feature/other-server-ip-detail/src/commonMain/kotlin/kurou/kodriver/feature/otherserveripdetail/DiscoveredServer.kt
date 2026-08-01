package kurou.kodriver.feature.otherserveripdetail

/**
 * mDNS で検出した KoDriver サーバーの表示名と接続先 IP アドレス。
 */
data class DiscoveredServer(
    val hostName: String,
    val ipAddress: String,
)
