package kurou.kodriver.feature.other

data class OtherListUiState(
    val items: List<String> = OtherListItemType.entries.map { it.id },
    val selectedItem: OtherListItemType? = null,
)
