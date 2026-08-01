@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5UdpPortUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherConsoleIpDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var addressRepository: FakeConsoleAddressPreferencesRepository
    private lateinit var portRepository: FakeGt7Ps5UdpPortPreferencesRepository
    private lateinit var viewModel: OtherConsoleIpDetailViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        addressRepository = FakeConsoleAddressPreferencesRepository(initial = "192.168.1.1")
        portRepository = FakeGt7Ps5UdpPortPreferencesRepository(initial = 33740)
        viewModel = buildViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        addrRepo: FakeConsoleAddressPreferencesRepository = addressRepository,
        portRepo: FakeGt7Ps5UdpPortPreferencesRepository = portRepository,
    ) = OtherConsoleIpDetailViewModel(
        observeConsoleAddress = ObserveConsoleAddressUseCase(addrRepo),
        saveConsoleAddress = SaveConsoleAddressUseCase(addrRepo),
        observeGt7Ps5UdpPort = ObserveGt7Ps5UdpPortUseCase(portRepo),
        saveGt7Ps5UdpPort = SaveGt7Ps5UdpPortUseCase(portRepo),
    )

    @Test
    fun `保存済みアドレスとポートを初期値として表示する`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals("192.168.1.1", state.inputAddress)
        assertTrue(state.isInputValid)
        assertEquals(33740, state.selectedPort)
    }

    @Test
    fun `保存済みポート33741が初期値として表示される`() = runTest {
        portRepository = FakeGt7Ps5UdpPortPreferencesRepository(initial = 33741)
        viewModel = buildViewModel()

        assertEquals(33741, viewModel.uiState.first().selectedPort)
    }

    @Test
    fun `正常なIPアドレスを保存できる`() = runTest {
        viewModel.onAddressChanged("10.0.0.5")
        viewModel.onSave()

        assertEquals("10.0.0.5", addressRepository.consoleAddress().first())
    }

    @Test
    fun `無効なIPアドレスは保存できない`() = runTest {
        viewModel.onAddressChanged("bad_ip")
        viewModel.onSave()

        assertEquals("192.168.1.1", addressRepository.consoleAddress().first())
        assertFalse(viewModel.uiState.first().isInputValid)
    }

    @Test
    fun `保存後にisSavedがtrueになる`() = runTest {
        viewModel.onAddressChanged("10.0.0.2")
        viewModel.onSave()

        assertTrue(viewModel.uiState.first().isSaved)
    }

    @Test
    fun `onDismissで状態がリセットされる`() = runTest {
        viewModel.onAddressChanged("10.0.0.99")
        viewModel.onDismiss()

        val state = viewModel.uiState.first()
        assertEquals("192.168.1.1", state.inputAddress)
        assertFalse(state.isSaved)
    }

    @Test
    fun `ポート33741を選択してから保存するとリポジトリに33741が保存される`() = runTest {
        viewModel.onAddressChanged("10.0.0.1")
        viewModel.onPortSelected(33741)
        viewModel.onSave()

        assertEquals(33741, portRepository.port().first())
    }

    @Test
    fun `ポートを選択するとuiStateのselectedPortが更新される`() = runTest {
        viewModel.onPortSelected(33741)
        assertEquals(33741, viewModel.uiState.first().selectedPort)

        viewModel.onPortSelected(33740)
        assertEquals(33740, viewModel.uiState.first().selectedPort)
    }

    @Test
    fun `onDismissでポート選択もリセットされる`() = runTest {
        viewModel.onPortSelected(33741)
        viewModel.onDismiss()

        assertEquals(33740, viewModel.uiState.first().selectedPort)
    }
}

private class FakeConsoleAddressPreferencesRepository(
    initial: String? = null,
) : ConsoleAddressPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun consoleAddress(): Flow<String?> = flow

    override suspend fun saveConsoleAddress(address: String) {
        flow.update { address }
    }
}

private class FakeGt7Ps5UdpPortPreferencesRepository(
    initial: Int = 33740,
) : Gt7Ps5UdpPortPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun port(): Flow<Int> = flow

    override suspend fun savePort(port: Int) {
        flow.update { port }
    }
}
