package kurou.kodriver.presentation

/**
 * ローカルネットワークの権限が未許可なら、接続状態の表示を「権限が必要」の案内に差し替える。
 * 許可済み（権限が不要なプラットフォームを含む）なら [this] をそのまま返す。
 * 通常の接続状態と混同しないよう、接続済み・待機中のスナックバーが出ない [ConnectionBannerStatus.UNCHECKED] にする。
 */
fun ConnectionBannerUiState.withAccessLocalNetworkPermission(
    granted: Boolean,
    requiredMessage: String,
): ConnectionBannerUiState =
    if (granted) {
        this
    } else {
        copy(
            status = ConnectionBannerStatus.UNCHECKED,
            message = requiredMessage,
            iconType = ConnectionBannerIconType.NETWORK,
            isTappable = true,
            tapNavigationTarget = ConnectionBannerNavigationTarget.AccessLocalNetworkPermission,
        )
    }
