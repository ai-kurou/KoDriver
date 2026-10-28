package kurou.kodriver.feature.lmuwindowsconnection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.model.Simulator

/** 選択中のシミュレーターに応じて LMU の接続表示状態を生成する冷たい Flow。購読の寿命は呼び出し元が管理する。 */
internal class LmuWindowsConnectionFlow(
    selectedSimulator: Flow<Simulator>,
    private val observeLmuWindowsConnection: () -> Flow<Boolean>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: Flow<LmuWindowsConnectionUiState> =
        selectedSimulator
            .flatMapLatest { simulator ->
                if (simulator is Simulator.LmuWindows) {
                    observeLmuWindowsConnection().map { isConnected ->
                        LmuWindowsConnectionUiState(
                            connectionStatus =
                                if (isConnected) {
                                    LmuWindowsConnectionStatus.CONNECTED
                                } else {
                                    LmuWindowsConnectionStatus.DISCONNECTED
                                },
                        )
                    }
                } else {
                    flowOf(LmuWindowsConnectionUiState())
                }
            }
}
