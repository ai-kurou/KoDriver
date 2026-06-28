package kurou.kodriver.presentation

internal fun connectionBannerNavigationTarget(
    isGt7: Boolean,
    supportsLmuServerIpNavigation: Boolean,
): ConnectionBannerNavigationTarget? = when {
    isGt7 -> ConnectionBannerNavigationTarget.ConsoleIp
    supportsLmuServerIpNavigation -> ConnectionBannerNavigationTarget.ServerIp
    else -> null
}
