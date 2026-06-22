@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.othergt7ps5ipdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.Gt7Ps5AddressRepository
import kurou.kodriver.domain.usecase.ObserveGt7Ps5AddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5AddressUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherGt7Ps5IpDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeGt7Ps5AddressRepository
    private lateinit var viewModel: OtherGt7Ps5IpDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeGt7Ps5AddressRepository(initial = "192.168.1.1")
        viewModel = buildViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        repo: FakeGt7Ps5AddressRepository = repository,
    ) = OtherGt7Ps5IpDetailViewModel(
        observeGt7Ps5Address = ObserveGt7Ps5AddressUseCase(repo),
        saveGt7Ps5Address = SaveGt7Ps5AddressUseCase(repo),
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

        assertEquals("10.0.0.5", repository.gt7Ps5Address().first())
    }

    @Test
    fun `無効なIPアドレスは保存できない`() = runTest {
        viewModel.onAddressChanged("bad_ip")
        viewModel.onSave()

        assertEquals("192.168.1.1", repository.gt7Ps5Address().first())
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

private class FakeGt7Ps5AddressRepository(initial: String? = null) : Gt7Ps5AddressRepository {
    private val flow = MutableStateFlow(initial)
    override fun gt7Ps5Address(): Flow<String?> = flow
    override suspend fun saveGt7Ps5Address(address: String) { flow.value = address }
}
