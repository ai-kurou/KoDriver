package kurou.kodriver.feature.otherlist

data class OtherListUiState(
    val items: List<String> = buildOtherListItems().map { it.id },
    val selectedItem: OtherListItemType? = null,
)
