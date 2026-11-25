package kurou.kodriver.domain.repository

/** 接続先の KoDriver サーバーへ到達できるかを確認する。 */
public fun interface ServerConnectivityRepository {
    public suspend fun isReachable(ip: String): Boolean
}
