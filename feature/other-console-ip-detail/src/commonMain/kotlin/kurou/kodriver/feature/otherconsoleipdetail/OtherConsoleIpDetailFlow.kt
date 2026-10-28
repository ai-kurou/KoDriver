package kurou.kodriver.feature.otherconsoleipdetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 保存済みIPとUDPポート、操作状態から画面状態を合成する冷たい Flow。 */
internal class OtherConsoleIpDetailFlow(
    savedAddress: Flow<String>,
    savedPort: Flow<Int>,
    mutableState: Flow<OtherConsoleIpDetailMutableState>,
) {
    val initialUiState = OtherConsoleIpDetailUiState()

    val uiState: Flow<OtherConsoleIpDetailUiState> =
        combine(
            savedAddress,
            savedPort,
            mutableState,
        ) { saved, port, m ->
            val current = m.userInput ?: saved
            OtherConsoleIpDetailUiState(
                inputAddress = current,
                isInputValid = current.isEmpty() || isValidConsoleIpAddress(current),
                saveFailed = m.saveFailed,
                isSaved = m.isSaved,
                selectedPort = m.userSelectedPort ?: port,
            )
        }
}

/** 画面状態と保存操作で共通のIP形式判定。空入力は保存不可とする。 */
internal fun isValidConsoleIpAddress(ip: String): Boolean {
    val parts = ip.split(".")
    if (parts.size != 4) return false
    return parts.all { part -> part.toIntOrNull()?.let { it in 0..255 } == true }
}
