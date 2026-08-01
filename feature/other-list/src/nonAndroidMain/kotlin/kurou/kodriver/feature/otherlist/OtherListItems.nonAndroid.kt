package kurou.kodriver.feature.otherlist

/**
 * buildOtherListItems のこのプラットフォーム向け実装。
 */
actual fun buildOtherListItems(): List<OtherListItemType> =
    OtherListItemType.entries.filter {
        it != OtherListItemType.ServerIp &&
            it != OtherListItemType.KeepScreenOn &&
            it != OtherListItemType.DynamicColor &&
            it != OtherListItemType.DebugState
    }
