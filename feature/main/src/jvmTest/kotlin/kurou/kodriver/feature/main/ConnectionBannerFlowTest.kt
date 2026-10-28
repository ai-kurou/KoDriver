package kurou.kodriver.feature.main

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionBannerFlowTest {
    private val lmuChecker: LmuBannerConnectionChecker = mockk()
    private val aceChecker: AceBannerConnectionChecker = mockk()

    @Test
    fun `購読時だけ選択中のステータスを監視し切り替え時に旧購読を解除する`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.LmuWindows)
            val lmuStatus = MutableStateFlow(ConnectionBannerVmStatus.IP_NOT_CONFIGURED)
            val aceStatus = MutableStateFlow(ConnectionBannerVmStatus.UNCHECKED)
            every { lmuChecker.statusFlow() } returns lmuStatus
            every { aceChecker.statusFlow() } returns aceStatus
            val states = mutableListOf<ConnectionBannerVmUiState>()
            val flow = createFlow(selectedSimulator)
            assertEquals(0, lmuStatus.subscriptionCount.value)
            verify(exactly = 0) { lmuChecker.statusFlow() }
            verify(exactly = 0) { aceChecker.statusFlow() }

            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.collect { states += it }
                }
            runCurrent()
            assertEquals(ConnectionBannerVmUiState(lmuStatus.value, Simulator.LmuWindows), states.last())
            lmuStatus.update { ConnectionBannerVmStatus.CONNECTED }
            runCurrent()
            assertEquals(ConnectionBannerVmUiState(lmuStatus.value, Simulator.LmuWindows), states.last())

            selectedSimulator.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(0, lmuStatus.subscriptionCount.value)
            assertEquals(1, aceStatus.subscriptionCount.value)
            assertEquals(ConnectionBannerVmUiState(aceStatus.value, Simulator.AceWindows), states.last())
            val count = states.size
            lmuStatus.update { ConnectionBannerVmStatus.DISCONNECTED }
            runCurrent()
            assertEquals(count, states.size)
            aceStatus.update { ConnectionBannerVmStatus.DISCONNECTED }
            runCurrent()
            assertEquals(ConnectionBannerVmUiState(aceStatus.value, Simulator.AceWindows), states.last())

            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(0, aceStatus.subscriptionCount.value)
            assertEquals(1, lmuStatus.subscriptionCount.value)
            job.cancel()
            runCurrent()
            assertEquals(0, lmuStatus.subscriptionCount.value)
            verify(exactly = 2) { lmuChecker.statusFlow() }
            verify(exactly = 1) { aceChecker.statusFlow() }
            confirmVerified(lmuChecker, aceChecker)
        }

    @Test
    fun `GT7はIP設定変更で周期チェックを再開し未設定とシミュレーター変更で停止する`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val address = MutableStateFlow<String?>(null)
            var checks = 0
            val states = mutableListOf<ConnectionBannerVmUiState>()
            val flow = createFlow(selectedSimulator, address) { ++checks % 2 == 1 }
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.collect { states += it }
                }
            runCurrent()
            assertEquals(
                ConnectionBannerVmUiState(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, Simulator.Gt7Ps5),
                states.last(),
            )
            assertEquals(0, checks)

            address.update { "192.168.1.1" }
            runCurrent()
            assertEquals(ConnectionBannerVmUiState(ConnectionBannerVmStatus.CONNECTED, Simulator.Gt7Ps5), states.last())
            advanceTimeBy(CONNECTION_CHECK_INTERVAL_MS_DEFAULT - 1)
            runCurrent()
            assertEquals(1, checks)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(ConnectionBannerVmStatus.DISCONNECTED, states.last().connectionStatus)
            assertEquals(2, checks)

            address.update { "192.168.1.2" }
            runCurrent()
            assertEquals(3, checks)
            address.update { null }
            runCurrent()
            advanceTimeBy(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
            runCurrent()
            assertEquals(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, states.last().connectionStatus)
            assertEquals(3, checks)

            address.update { "" }
            runCurrent()
            assertEquals(4, checks)
            val lmuStatus = MutableStateFlow(ConnectionBannerVmStatus.CONNECTED)
            every { lmuChecker.statusFlow() } returns lmuStatus
            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()
            address.update { "192.168.1.3" }
            advanceTimeBy(CONNECTION_CHECK_INTERVAL_MS_DEFAULT * 2)
            runCurrent()
            assertEquals(4, checks)
            assertEquals(0, address.subscriptionCount.value)
            assertEquals(
                ConnectionBannerVmUiState(ConnectionBannerVmStatus.CONNECTED, Simulator.LmuWindows),
                states.last(),
            )
            job.cancel()
            verify(exactly = 1) { lmuChecker.statusFlow() }
            verify(exactly = 0) { aceChecker.statusFlow() }
            confirmVerified(lmuChecker, aceChecker)
        }

    @Test
    fun `GT7チェック例外は未接続として通知し次の周期で回復する`() =
        runTest {
            var checks = 0
            val states = mutableListOf<ConnectionBannerVmUiState>()
            val flow =
                createFlow(MutableStateFlow(Simulator.Gt7Ps5), MutableStateFlow("192.168.1.1")) {
                    if (++checks == 1) error("connection check failed")
                    true
                }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }
            runCurrent()
            assertEquals(ConnectionBannerVmStatus.DISCONNECTED, states.last().connectionStatus)
            advanceTimeBy(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
            runCurrent()
            assertEquals(ConnectionBannerVmStatus.CONNECTED, states.last().connectionStatus)
            assertEquals(2, checks)
        }

    @Test
    fun `GT7チェックのCancellationExceptionを未接続に変換せず監視を中断する`() =
        runTest {
            var checks = 0
            val states = mutableListOf<ConnectionBannerVmUiState>()
            val flow =
                createFlow(MutableStateFlow(Simulator.Gt7Ps5), MutableStateFlow("192.168.1.1")) {
                    checks++
                    throw CancellationException("cancel check")
                }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }
            runCurrent()
            advanceTimeBy(CONNECTION_CHECK_INTERVAL_MS_DEFAULT * 2)
            runCurrent()
            assertTrue(states.isEmpty())
            assertEquals(1, checks)
        }

    @Test
    fun `シミュレーター変更は実行中のGT7チェックをキャンセルする`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val address = MutableStateFlow<String?>("192.168.1.1")
            var cancelled = false
            every { aceChecker.statusFlow() } returns MutableStateFlow(ConnectionBannerVmStatus.CONNECTED)
            val flow =
                createFlow(selectedSimulator, address) {
                    try {
                        awaitCancellation()
                    } finally {
                        cancelled = true
                    }
                }
            val states = mutableListOf<ConnectionBannerVmUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }
            runCurrent()
            selectedSimulator.update { Simulator.AceWindows }
            runCurrent()
            assertTrue(cancelled)
            assertEquals(
                ConnectionBannerVmUiState(ConnectionBannerVmStatus.CONNECTED, Simulator.AceWindows),
                states.single(),
            )
            assertEquals(0, address.subscriptionCount.value)
            verify(exactly = 0) { lmuChecker.statusFlow() }
            verify(exactly = 1) { aceChecker.statusFlow() }
            confirmVerified(lmuChecker, aceChecker)
        }

    @Test
    fun `冷たいFlowは再購読ごとにGT7チェックを実行する`() =
        runTest {
            var checks = 0
            val flow =
                createFlow(MutableStateFlow(Simulator.Gt7Ps5), MutableStateFlow("192.168.1.1")) {
                    checks++
                    true
                }
            assertEquals(0, checks)
            repeat(2) {
                assertEquals(ConnectionBannerVmStatus.CONNECTED, flow.uiState.first().connectionStatus)
            }
            assertEquals(2, checks)
        }

    private fun createFlow(
        selectedSimulator: Flow<Simulator>,
        consoleAddress: Flow<String?> = flow { error("GT7以外では購読しない") },
        checkGt7Ps5Connection: suspend () -> Boolean = { error("GT7以外ではチェックしない") },
    ) = ConnectionBannerFlow(
        selectedSimulator = selectedSimulator,
        observeConsoleAddress = { consoleAddress },
        checkLmuConnection = lmuChecker,
        checkAceConnection = aceChecker,
        checkGt7Ps5Connection = checkGt7Ps5Connection,
    )
}
