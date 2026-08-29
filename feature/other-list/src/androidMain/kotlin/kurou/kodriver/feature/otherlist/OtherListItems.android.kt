package kurou.kodriver.feature.otherlist

import android.os.Build

/**
 * buildOtherListItems のこのプラットフォーム向け実装。
 */
actual fun buildOtherListItems(): List<OtherListItemType> {
    val items =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            OtherListItemType.entries.filter {
                it != OtherListItemType.DebugState && it != OtherListItemType.Startup
            }
        } else {
            OtherListItemType.entries.filter {
                it != OtherListItemType.DynamicColor &&
                    it != OtherListItemType.DebugState &&
                    it != OtherListItemType.Startup
            }
        }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        items
    } else {
        items.filter { it != OtherListItemType.AccessLocalNetworkPermission }
    }
}
