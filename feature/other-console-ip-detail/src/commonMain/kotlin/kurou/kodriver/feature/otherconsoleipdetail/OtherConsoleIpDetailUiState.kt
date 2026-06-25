package kurou.kodriver.feature.otherconsoleipdetail

data class OtherConsoleIpDetailUiState(
    val inputAddress: String = "",
    val isInputValid: Boolean = true,
    val saveFailed: Boolean = false,
    val isSaved: Boolean = false,
    val selectedPort: Int = 33740,
)
