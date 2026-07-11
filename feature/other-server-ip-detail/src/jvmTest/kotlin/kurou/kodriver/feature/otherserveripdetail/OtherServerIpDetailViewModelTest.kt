@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherServerIpDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeServerIpPreferencesRepository
    private lateinit var viewModel: OtherServerIpDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeServerIpPreferencesRepository(initialIp = "192.168.1.1")
        viewModel = buildViewModel(reachable = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        reachable: Boolean = true,
        repo: FakeServerIpPreferencesRepository = repository,
    ) = OtherServerIpDetailViewModel(
        observeServerIp = ObserveServerIpUseCase(repo),
        saveServerIp = SaveServerIpUseCase(repo),
        connectivityChecker = ServerConnectivityChecker { reachable },
    )

    @Test
    fun `保存済みのIPアドレスをUiStateで返す`() = runTest {
        val expected = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isInputValid = true)
        assertEquals(expected, viewModel.uiState.first())
    }

    @Test
    fun `IPアドレスを変更するとUiStateが更新される`() = runTest {
        viewModel.onIpChanged("10.0.0.1")

        assertEquals(OtherServerIpDetailUiState(inputIp = "10.0.0.1", isInputValid = true), viewModel.uiState.first())
    }

    @Test
    fun `不正なIPアドレスを入力するとisInputValidがfalseになる`() = runTest {
        viewModel.onIpChanged("invalid")

        assertFalse(viewModel.uiState.first().isInputValid)
    }

    @Test
    fun `サーバーに到達可能な場合は保存されisSavedがtrueになる`() = runTest {
        viewModel.onIpChanged("10.0.0.2")
        viewModel.onSave()

        assertEquals("10.0.0.2", repository.serverIp().first())
        assertTrue(viewModel.uiState.first().isSaved)
    }

    @Test
    fun `不正なIPアドレスは保存されない`() = runTest {
        viewModel.onIpChanged("bad")
        viewModel.onSave()

        assertEquals("192.168.1.1", repository.serverIp().first())
    }

    @Test
    fun `サーバーに到達不可能な場合は警告が表示されisSavedはfalseのまま`() = runTest {
        val vm = buildViewModel(reachable = false)
        vm.onIpChanged("10.0.0.1")
        vm.onSave()

        assertTrue(vm.uiState.first().connectivityWarning)
        assertFalse(vm.uiState.first().isSaved)
    }

    @Test
    fun `到達不可能でもonSaveAnywayで保存できる`() = runTest {
        val vm = buildViewModel(reachable = false)
        vm.onIpChanged("10.0.0.1")
        vm.onSave()
        vm.onSaveAnyway()

        assertEquals("10.0.0.1", repository.serverIp().first())
        assertTrue(vm.uiState.first().isSaved)
    }

    @Test
    fun `onDismissで入力内容と警告がリセットされる`() = runTest {
        val vm = buildViewModel(reachable = false)
        vm.onIpChanged("10.0.0.99")
        vm.onSave()
        vm.onDismiss()

        val state = vm.uiState.first()
        assertEquals("192.168.1.1", state.inputIp)
        assertFalse(state.connectivityWarning)
        assertFalse(state.isSaved)
    }

    @Test
    fun `保存済みIPがない場合は空文字を返す`() = runTest {
        val fakeRepo = FakeServerIpPreferencesRepository(initialIp = null)
        val vm = buildViewModel(repo = fakeRepo)

        assertEquals(OtherServerIpDetailUiState(inputIp = "", isInputValid = true), vm.uiState.first())
    }

    @Test
    fun `255を超える値を持つIPアドレスは不正と判定される`() = runTest {
        viewModel.onIpChanged("256.0.0.1")

        assertFalse(viewModel.uiState.first().isInputValid)
    }

    @Test
    fun `境界値の255は有効と判定される`() = runTest {
        viewModel.onIpChanged("255.255.255.255")

        assertTrue(viewModel.uiState.first().isInputValid)
    }

    @Test
    fun `保存に失敗するとsaveFailedがtrueになる`() = runTest {
        val fakeRepo = FakeServerIpPreferencesRepository(initialIp = "192.168.1.1", shouldThrow = true)
        val vm = buildViewModel(repo = fakeRepo)
        vm.onIpChanged("10.0.0.1")
        vm.onSave()

        assertTrue(vm.uiState.first().saveFailed)
    }

    @Test
    fun `保存失敗後に再度保存が成功するとsaveFailedがfalseにリセットされる`() = runTest {
        val fakeRepo = FakeServerIpPreferencesRepository(initialIp = "192.168.1.1", shouldThrow = true)
        val vm = buildViewModel(repo = fakeRepo)
        vm.onIpChanged("10.0.0.1")
        vm.onSave()

        fakeRepo.shouldThrow = false
        vm.onSave()

        assertFalse(vm.uiState.first().saveFailed)
    }
}
