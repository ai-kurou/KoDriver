package kurou.kodriver.domain.serverconnection.model

/**
 * mDNS で検出した KoDriver サーバーの表示名と接続先 IPアドレス。
 */
public data class DiscoveredServer(
    val hostName: String,
    val ipAddress: String,
)
