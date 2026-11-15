package kurou.kodriver.feature.otherlist

/**
 * buildOtherListItems のこのプラットフォーム向け実装。
 */
internal actual fun buildOtherListItems(): List<OtherListItemType> =
    OtherListItemType.entries.filter {
        it != OtherListItemType.AccessLocalNetworkPermission &&
            it != OtherListItemType.AndroidAudioUsage &&
            it != OtherListItemType.ServerIp &&
            it != OtherListItemType.KeepScreenOn &&
            it != OtherListItemType.DynamicColor &&
            it != OtherListItemType.HapticFeedback &&
            it != OtherListItemType.DebugState &&
            it != OtherListItemType.TtsEngineMissing &&
            it != OtherListItemType.TtsLanguageDataMissing &&
            it != OtherListItemType.WindowsSpeechUnavailable
    }
