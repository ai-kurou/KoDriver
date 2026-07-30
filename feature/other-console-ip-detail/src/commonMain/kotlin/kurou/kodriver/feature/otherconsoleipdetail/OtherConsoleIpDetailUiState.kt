package kurou.kodriver.feature.otherconsoleipdetail

import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT

data class OtherConsoleIpDetailUiState(
    val inputAddress: String = "",
    val isInputValid: Boolean = true,
    val saveFailed: Boolean = false,
    val isSaved: Boolean = false,
    val selectedPort: Int = GT7_PS5_UDP_PORT_DEFAULT,
)
