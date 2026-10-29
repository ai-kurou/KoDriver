package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OtherServerIpDetailFlowTest {
    private val savedIp = MutableStateFlow<String?>("192.168.1.1")
    private val mutableState = MutableStateFlow(OtherServerIpDetailMutableState())
    private val discoveredServers = MutableStateFlow<List<DiscoveredServer>>(emptyList())
    private val serverA = DiscoveredServer("DESKTOP-A", "192.168.1.10")
    private val serverB = DiscoveredServer("DESKTOP-B", "192.168.1.20")

    private fun createFlow() =
        OtherServerIpDetailFlow(
            savedIp = savedIp,
            mutableState = mutableState,
            discoveredServers = discoveredServers,
            validateServerIpAddress = ValidateServerIpAddressUseCase(),
        )

    @Test
    fun `初期状態と保存済みIPのフォールバックを返す`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherServerIpDetailUiState(), flow.initialUiState)
            assertEquals(OtherServerIpDetailUiState(inputIp = "192.168.1.1"), flow.uiState.first())

            savedIp.update { "" }
            assertEquals(OtherServerIpDetailUiState(), flow.uiState.first())
            savedIp.update { "10.0.0.1" }
            assertEquals("10.0.0.1", flow.uiState.first().inputIp)
        }

    @Test
    fun `保存済みIPがnullなら空文字として表示し保存済みIPの変化も反映する`() =
        runTest {
            savedIp.update { null }
            val flow = createFlow()

            assertEquals(OtherServerIpDetailUiState(), flow.uiState.first())
            savedIp.update { "10.0.0.1" }
            assertEquals(OtherServerIpDetailUiState(inputIp = "10.0.0.1"), flow.uiState.first())
            savedIp.update { null }
            assertEquals(OtherServerIpDetailUiState(), flow.uiState.first())
        }

    @Test
    fun `保存済みIPがnullでも入力を優先しリセットすると空文字へ戻る`() =
        runTest {
            savedIp.update { null }
            val flow = createFlow()
            mutableState.update { it.copy(userInput = "10.0.0.1") }

            assertEquals(OtherServerIpDetailUiState(inputIp = "10.0.0.1"), flow.uiState.first())
            mutableState.update { OtherServerIpDetailMutableState() }
            assertEquals(OtherServerIpDetailUiState(), flow.uiState.first())
        }

    @Test
    fun `入力を優先して空文字とIPの形式と境界値を検証する`() =
        runTest {
            val flow = createFlow()
            val inputs =
                listOf("" to true, "invalid" to false, "256.0.0.1" to false, "255.255.255.255" to true)
            for ((input, valid) in inputs) {
                mutableState.update { it.copy(userInput = input) }
                assertEquals(OtherServerIpDetailUiState(inputIp = input, isInputValid = valid), flow.uiState.first())
            }
            savedIp.update { "10.0.0.2" }
            assertEquals("255.255.255.255", flow.uiState.first().inputIp)
            mutableState.update { it.copy(userInput = null) }
            assertEquals("10.0.0.2", flow.uiState.first().inputIp)
            savedIp.update { "invalid" }
            assertFalse(flow.uiState.first().isInputValid)
        }

    @Test
    fun `未選択時は検出結果の先頭を使い明示選択は検出結果から消えても保持する`() =
        runTest {
            val flow = createFlow()
            assertEquals(null, flow.uiState.first().selectedDiscoveredServer)
            discoveredServers.update { listOf(serverA, serverB) }
            assertEquals(serverA, flow.uiState.first().selectedDiscoveredServer)
            discoveredServers.update { listOf(serverB, serverA) }
            assertEquals(serverB, flow.uiState.first().selectedDiscoveredServer)

            mutableState.update { it.copy(selectedDiscoveredServer = serverA) }
            discoveredServers.update { listOf(serverB) }
            assertEquals(serverA, flow.uiState.first().selectedDiscoveredServer)
            discoveredServers.update { emptyList() }
            assertEquals(serverA, flow.uiState.first().selectedDiscoveredServer)
            mutableState.update { it.copy(selectedDiscoveredServer = null) }
            assertEquals(null, flow.uiState.first().selectedDiscoveredServer)
        }

    @Test
    fun `検出結果と保存済みとダイアログ終了状態から表示を判定する`() =
        runTest {
            val flow = createFlow()
            for (saved in listOf(false, true)) {
                for (dismissed in listOf(false, true)) {
                    for (servers in listOf(emptyList(), listOf(serverA))) {
                        mutableState.update { it.copy(isSaved = saved, isDiscoveryDialogDismissed = dismissed) }
                        discoveredServers.update { servers }
                        val state = flow.uiState.first()
                        assertEquals(!saved && !dismissed && servers.isNotEmpty(), state.isDiscoveryDialogVisible)
                        assertEquals(saved, state.isSaved)
                        assertEquals(servers, state.discoveredServers)
                    }
                }
            }
            mutableState.update { OtherServerIpDetailMutableState() }
            assertTrue(flow.uiState.first().isDiscoveryDialogVisible)
        }

    @Test
    fun `保存と疎通確認の操作状態を反映しリセットすると保存済みIPへ戻る`() =
        runTest {
            val flow = createFlow()
            mutableState.update {
                it.copy(
                    userInput = "10.0.0.1",
                    saveFailed = true,
                    isCheckingConnectivity = true,
                    connectivityWarning = true,
                )
            }
            assertEquals(
                OtherServerIpDetailUiState(
                    inputIp = "10.0.0.1",
                    saveFailed = true,
                    isCheckingConnectivity = true,
                    connectivityWarning = true,
                ),
                flow.uiState.first(),
            )
            mutableState.update { OtherServerIpDetailMutableState() }
            assertEquals(OtherServerIpDetailUiState(inputIp = "192.168.1.1"), flow.uiState.first())
        }
}
