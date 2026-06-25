package kurou.kodriver.feature.otherlist

actual fun buildOtherListItems(): List<OtherListItemType> =
    OtherListItemType.entries.filter {
        it != OtherListItemType.ServerIp && it != OtherListItemType.KeepScreenOn
    }
