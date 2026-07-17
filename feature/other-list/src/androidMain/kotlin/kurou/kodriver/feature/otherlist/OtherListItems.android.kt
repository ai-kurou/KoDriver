package kurou.kodriver.feature.otherlist

import android.os.Build

actual fun buildOtherListItems(): List<OtherListItemType> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        OtherListItemType.entries
    } else {
        OtherListItemType.entries.filter { it != OtherListItemType.DynamicColor }
    }
