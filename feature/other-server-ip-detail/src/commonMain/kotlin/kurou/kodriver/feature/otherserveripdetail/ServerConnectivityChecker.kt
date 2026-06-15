package kurou.kodriver.feature.otherserveripdetail

internal fun interface ServerConnectivityChecker {
    suspend fun isReachable(ip: String): Boolean
}

internal expect fun createServerConnectivityChecker(): ServerConnectivityChecker
