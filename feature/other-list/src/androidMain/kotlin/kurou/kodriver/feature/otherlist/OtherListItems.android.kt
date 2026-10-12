package kurou.kodriver.feature.otherlist

import android.os.Build

/**
 * buildOtherListItems のこのプラットフォーム向け実装。
 */
actual fun buildOtherListItems(): List<OtherListItemType> {
    val excludedItems =
        setOf(
            OtherListItemType.DebugState,
            OtherListItemType.WindowsSpeechUnavailable,
            OtherListItemType.Startup,
            OtherListItemType.OverlayVisible,
            OtherListItemType.OverlayTextSize,
            OtherListItemType.OverlayTextColor,
            OtherListItemType.OverlayBackgroundOpacity,
        )
    val items =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            OtherListItemType.entries.filter { it !in excludedItems }
        } else {
            OtherListItemType.entries.filter { it !in excludedItems && it != OtherListItemType.DynamicColor }
        }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        items
    } else {
        items.filter { it != OtherListItemType.AccessLocalNetworkPermission }
    }
}
