package kurou.kodriver.feature.otherserveripdetail

internal actual fun createServerConnectivityChecker(): ServerConnectivityChecker =
    ServerConnectivityChecker { true }
