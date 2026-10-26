package kurou.kodriver.feature.otherlist

import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_UDP_PORT_DEFAULT

/** 接続情報の表示状態。 */
data class OtherListConnectionUiState(
    val serverIp: String? = null,
    val consoleAddress: String? = null,
    val consolePort: Int = GT7_PS5_UDP_PORT_DEFAULT,
)
