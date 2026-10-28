package kurou.kodriver.feature.acewindowsconnection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.acewindows.usecase.AceWindowsConnectionState
import kurou.kodriver.domain.model.Simulator

/** 選択中のシミュレーターに応じてACEの接続状態を表示状態へ変換する冷たい Flow。 */
internal class AceWindowsConnectionFlow(
    selectedSimulator: Flow<Simulator>,
    private val observeAceWindowsConnection: () -> Flow<AceWindowsConnectionState>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: Flow<AceWindowsConnectionUiState> =
        selectedSimulator.flatMapLatest { simulator ->
            if (simulator is Simulator.AceWindows) {
                observeAceWindowsConnection().map { state ->
                    AceWindowsConnectionUiState(
                        connectionStatus =
                            if (state.isConnected) {
                                AceWindowsConnectionStatus.CONNECTED
                            } else {
                                AceWindowsConnectionStatus.DISCONNECTED
                            },
                        fuelRemainingPercent = state.fuel?.remainingPercent?.value,
                    )
                }
            } else {
                flowOf(AceWindowsConnectionUiState())
            }
        }
}
