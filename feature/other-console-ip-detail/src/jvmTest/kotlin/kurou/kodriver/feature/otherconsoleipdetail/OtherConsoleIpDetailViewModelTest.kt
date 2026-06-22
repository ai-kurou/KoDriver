@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherconsoleipdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherConsoleIpDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeConsoleAddressRepository
    private lateinit var viewModel: OtherConsoleIpDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeConsoleAddressRepository(initial = "192.168.1.1")
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        repo: FakeConsoleAddressRepository = repository,
    ) = OtherConsoleIpDetailViewModel(
        observeConsoleAddress = ObserveConsoleAddressUseCase(repo),
        saveConsoleAddress = SaveConsoleAddressUseCase(repo),
    )

    @Test
    fun `保存済みアドレスを初期値として表示する`() = runTest {
        val state = viewModel.uiState.first()
        assertEquals("192.168.1.1", state.inputAddress)
        assertTrue(state.isInputValid)
    }

    @Test
    fun `正常なIPアドレスを保存できる`() = runTest {
        viewModel.onAddressChanged("10.0.0.5")
        viewModel.onSave()

        assertEquals("10.0.0.5", repository.consoleAddress().first())
    }

    @Test
    fun `無効なIPアドレスは保存できない`() = runTest {
        viewModel.onAddressChanged("bad_ip")
        viewModel.onSave()

        assertEquals("192.168.1.1", repository.consoleAddress().first())
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
}

private class FakeConsoleAddressRepository(initial: String? = null) : ConsoleAddressRepository {
    private val flow = MutableStateFlow(initial)
    override fun consoleAddress(): Flow<String?> = flow
    override suspend fun saveConsoleAddress(address: String) { flow.value = address }
}
