package kurou.kodriver.feature.otherlist

data class OtherListUiState(
    val items: List<OtherListItemType> = buildOtherListItems(),
    val selectedItem: OtherListItemType? = null,
    val hasAppUpdate: Boolean = false,
    val keepScreenOn: Boolean = true,
    val exitConfirmationEnabled: Boolean = true,
    val dynamicColorEnabled: Boolean = false,
    val appVersionLabel: String = "",
    val appVersion: String = "",
)
