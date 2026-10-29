package kurou.kodriver.feature.otherconsoleipdetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OtherConsoleIpDetailFlowTest {
    private val savedAddress = MutableStateFlow<String?>("192.168.1.1")
    private val savedPort = MutableStateFlow(33740)
    private val mutableState = MutableStateFlow(OtherConsoleIpDetailMutableState())

    private fun createFlow() =
        OtherConsoleIpDetailFlow(
            savedAddress = savedAddress,
            savedPort = savedPort,
            mutableState = mutableState,
        )

    @Test
    fun `初期状態と保存済みIPのフォールバックを返す`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherConsoleIpDetailUiState(), flow.initialUiState)
            assertEquals(OtherConsoleIpDetailUiState(inputAddress = "192.168.1.1"), flow.uiState.first())
            savedAddress.update { "" }
            assertEquals(OtherConsoleIpDetailUiState(), flow.uiState.first())
            savedAddress.update { "10.0.0.1" }
            assertEquals("10.0.0.1", flow.uiState.first().inputAddress)
            savedAddress.update { "invalid" }
            assertFalse(flow.uiState.first().isInputValid)
        }

    @Test
    fun `保存済みIPがnullなら空文字へフォールバックする`() =
        runTest {
            savedAddress.update { null }
            val flow = createFlow()

            assertEquals(OtherConsoleIpDetailUiState(), flow.uiState.first())
            mutableState.update { it.copy(userInput = "10.0.0.1") }
            assertEquals(OtherConsoleIpDetailUiState(inputAddress = "10.0.0.1"), flow.uiState.first())
            mutableState.update { it.copy(userInput = null) }
            assertEquals(OtherConsoleIpDetailUiState(), flow.uiState.first())
            savedAddress.update { "192.168.1.2" }
            assertEquals(OtherConsoleIpDetailUiState(inputAddress = "192.168.1.2"), flow.uiState.first())
            savedAddress.update { null }
            assertEquals(OtherConsoleIpDetailUiState(), flow.uiState.first())
        }

    @Test
    fun `入力を優先して空文字と不正IPと境界値を検証する`() =
        runTest {
            val flow = createFlow()
            val inputs =
                listOf(
                    "" to true,
                    "invalid" to false,
                    "1.2.3" to false,
                    "1.2.3.4.5" to false,
                    "1..3.4" to false,
                    "-1.0.0.1" to false,
                    "256.0.0.1" to false,
                    "0.0.0.0" to true,
                    "255.255.255.255" to true,
                )
            for ((input, valid) in inputs) {
                mutableState.update { it.copy(userInput = input) }
                assertEquals(
                    OtherConsoleIpDetailUiState(inputAddress = input, isInputValid = valid),
                    flow.uiState.first(),
                )
                assertEquals(input.isNotEmpty() && valid, isValidConsoleIpAddress(input))
            }
            savedAddress.update { "10.0.0.2" }
            assertEquals("255.255.255.255", flow.uiState.first().inputAddress)
            mutableState.update { it.copy(userInput = null) }
            assertEquals("10.0.0.2", flow.uiState.first().inputAddress)
        }

    @Test
    fun `選択ポートを優先して未選択時は最新の保存ポートへ戻る`() =
        runTest {
            val flow = createFlow()
            assertEquals(33740, flow.uiState.first().selectedPort)
            savedPort.update { 33741 }
            assertEquals(33741, flow.uiState.first().selectedPort)
            mutableState.update { it.copy(userSelectedPort = 33740) }
            assertEquals(33740, flow.uiState.first().selectedPort)
            savedPort.update { 33740 }
            mutableState.update { it.copy(userSelectedPort = 33741) }
            assertEquals(33741, flow.uiState.first().selectedPort)
            mutableState.update { it.copy(userSelectedPort = null) }
            assertEquals(33740, flow.uiState.first().selectedPort)
        }

    @Test
    fun `保存状態を反映しリセットすると保存値へ戻る`() =
        runTest {
            val flow = createFlow()
            for (failed in listOf(false, true)) {
                for (saved in listOf(false, true)) {
                    mutableState.update {
                        it.copy(userInput = "10.0.0.1", userSelectedPort = 33741, saveFailed = failed, isSaved = saved)
                    }
                    assertEquals(
                        OtherConsoleIpDetailUiState(
                            inputAddress = "10.0.0.1",
                            selectedPort = 33741,
                            saveFailed = failed,
                            isSaved = saved,
                        ),
                        flow.uiState.first(),
                    )
                }
            }
            mutableState.update { OtherConsoleIpDetailMutableState() }
            assertEquals(OtherConsoleIpDetailUiState(inputAddress = "192.168.1.1"), flow.uiState.first())
        }
}
