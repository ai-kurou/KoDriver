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
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.repository.Gt7UdpPortPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7UdpPortUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherConsoleIpDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var addressRepository: FakeConsoleAddressRepository
    private lateinit var portRepository: FakeGt7UdpPortPreferencesRepository
    private lateinit var viewModel: OtherConsoleIpDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        addressRepository = FakeConsoleAddressRepository(initial = "192.168.1.1")
        portRepository = FakeGt7UdpPortPreferencesRepository(initial = 33740)
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        addrRepo: FakeConsoleAddressRepository = addressRepository,
        portRepo: FakeGt7UdpPortPreferencesRepository = portRepository,
    ) = OtherConsoleIpDetailViewModel(
        observeConsoleAddress = ObserveConsoleAddressUseCase(addrRepo),
        saveConsoleAddress = SaveConsoleAddressUseCase(addrRepo),
        observeGt7UdpPort = ObserveGt7UdpPortUseCase(portRepo),
        saveGt7UdpPort = SaveGt7UdpPortUseCase(portRepo),
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
        portRepository = FakeGt7UdpPortPreferencesRepository(initial = 33741)
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

private class FakeConsoleAddressRepository(initial: String? = null) : ConsoleAddressRepository {
    private val flow = MutableStateFlow(initial)
    override fun consoleAddress(): Flow<String?> = flow
    override suspend fun saveConsoleAddress(address: String) { flow.update { address } }
}

private class FakeGt7UdpPortPreferencesRepository(initial: Int = 33740) : Gt7UdpPortPreferencesRepository {
    private val flow = MutableStateFlow(initial)
    override fun port(): Flow<Int> = flow
    override suspend fun savePort(port: Int) { flow.update { port } }
}
