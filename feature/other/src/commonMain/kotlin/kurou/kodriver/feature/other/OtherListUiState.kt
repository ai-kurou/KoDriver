package kurou.kodriver.feature.other

data class OtherListUiState(
    val items: List<String> = OtherItemType.entries.map { it.id },
    val selectedItem: OtherItemType? = null,
)
