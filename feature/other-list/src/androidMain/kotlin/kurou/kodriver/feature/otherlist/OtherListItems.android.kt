package kurou.kodriver.feature.otherlist

import android.os.Build

/**
 * buildOtherListItems のこのプラットフォーム向け実装。
 */
actual fun buildOtherListItems(): List<OtherListItemType> {
    val sdkInt = Build.VERSION.SDK_INT
    return OtherListItemType.entries.filter { it !in excludedItems && isAvailableOnSdk(it, sdkInt) }
}

private val excludedItems =
    setOf(
        OtherListItemType.DebugState,
        OtherListItemType.WindowsSpeechUnavailable,
        OtherListItemType.Startup,
        OtherListItemType.OverlayVisible,
        OtherListItemType.OverlayTextSize,
        OtherListItemType.OverlayTextColor,
        OtherListItemType.OverlayBackgroundOpacity,
    )

private fun isAvailableOnSdk(
    item: OtherListItemType,
    sdkInt: Int,
): Boolean =
    when (item) {
        OtherListItemType.DynamicColor -> sdkInt >= Build.VERSION_CODES.S
        OtherListItemType.AccessLocalNetworkPermission -> sdkInt >= Build.VERSION_CODES.BAKLAVA
        else -> true
    }
