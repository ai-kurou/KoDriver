package kurou.kodriver.feature.serverconnection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.KoDriverServerConnectionState
import kurou.kodriver.domain.usecase.KoDriverServerConnectionStatus
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConnectionFlowTest {
    @Test
    fun `すべての接続状態を表示用ステータスに変換する`() =
        runTest {
            data class ExpectedState(
                val status: ServerConnectionStatus,
                val isConnected: Boolean,
                val isConnectionChecked: Boolean,
                val isIpConfigured: Boolean,
            )

            val statuses =
                listOf(
                    KoDriverServerConnectionStatus.NOT_CONFIGURED to
                        ExpectedState(
                            status = ServerConnectionStatus.NOT_CONFIGURED,
                            isConnected = false,
                            isConnectionChecked = false,
                            isIpConfigured = false,
                        ),
                    KoDriverServerConnectionStatus.CHECKING to
                        ExpectedState(
                            status = ServerConnectionStatus.CHECKING,
                            isConnected = false,
                            isConnectionChecked = false,
                            isIpConfigured = true,
                        ),
                    KoDriverServerConnectionStatus.CONNECTED to
                        ExpectedState(
                            status = ServerConnectionStatus.CONNECTED,
                            isConnected = true,
                            isConnectionChecked = true,
                            isIpConfigured = true,
                        ),
                    KoDriverServerConnectionStatus.DISCONNECTED to
                        ExpectedState(
                            status = ServerConnectionStatus.DISCONNECTED,
                            isConnected = false,
                            isConnectionChecked = true,
                            isIpConfigured = true,
                        ),
                )
            for ((status, expected) in statuses) {
                val flow =
                    ServerConnectionFlow(
                        connectionState = flowOf(KoDriverServerConnectionState(connectionStatus = status)),
                        showVersionMismatchBottomSheet = flowOf(false),
                        appVersion = "1.0.0",
                    )

                val state = flow.uiState.first()
                assertEquals(expected.status, state.connectionStatus, "status=$status")
                assertEquals(expected.isConnected, state.isConnected, "status=$status の isConnected")
                assertEquals(
                    expected.isConnectionChecked,
                    state.isConnectionChecked,
                    "status=$status の isConnectionChecked",
                )
                assertEquals(expected.isIpConfigured, state.isIpConfigured, "status=$status の isIpConfigured")
            }
        }

    @Test
    fun `接続情報と表示フラグの更新を合成しアプリバージョンを保持する`() =
        runTest {
            val connection = MutableStateFlow(KoDriverServerConnectionState())
            val visibility = MutableStateFlow(false)
            val flow = ServerConnectionFlow(connection, visibility, "1.0.0")
            assertEquals(ServerConnectionUiState(appVersion = "1.0.0"), flow.uiState.first())
            val expected =
                ServerConnectionUiState(
                    connectionStatus = ServerConnectionStatus.CONNECTED,
                    requiresKoDriverServer = false,
                    selectedSimulator = Simulator.Gt7Ps5,
                    serverVersion = "2.0.0",
                    appVersion = "1.0.0",
                )
            val updatedState =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first { it == expected }
                }

            connection.update {
                KoDriverServerConnectionState(
                    connectionStatus = KoDriverServerConnectionStatus.CONNECTED,
                    requiresKoDriverServer = false,
                    selectedSimulator = Simulator.Gt7Ps5,
                    serverVersion = "2.0.0",
                    isVersionMismatch = true,
                )
            }

            assertEquals(expected, updatedState.await())
            val visibleState =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first { it.showVersionMismatchBottomSheet }
                }
            visibility.update { true }
            assertEquals(expected.copy(showVersionMismatchBottomSheet = true), visibleState.await())
            val dismissedState =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.first { !it.showVersionMismatchBottomSheet }
                }
            visibility.update { false }
            assertEquals(expected, dismissedState.await())
        }

    @Test
    fun `購読時だけ入力を収集し購読ごとに再収集する`() =
        runTest {
            var collections = 0
            val connection =
                flow {
                    collections++
                    emit(KoDriverServerConnectionState())
                }
            val flow = ServerConnectionFlow(connection, flowOf(false), "1.0.0")

            assertEquals(0, collections)
            repeat(2) {
                assertEquals(ServerConnectionUiState(appVersion = "1.0.0"), flow.uiState.first())
            }
            assertEquals(2, collections)
        }
}
