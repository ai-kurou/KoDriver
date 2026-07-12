@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.otherserveripdetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase
import org.junit.After
import org.junit.Before
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherServerIpDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var repository: ServerIpPreferencesRepository

    private val ipFlow = MutableStateFlow<String?>("192.168.1.1")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(
        reachable: Boolean = true,
    ) = OtherServerIpDetailViewModel(
        observeServerIp = ObserveServerIpUseCase(repository),
        saveServerIp = SaveServerIpUseCase(repository),
        connectivityChecker = ServerConnectivityChecker { reachable },
    )

    @Test
    fun `保存済みのIPアドレスをUiStateで返す`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val viewModel = buildViewModel()

        val expected = OtherServerIpDetailUiState(inputIp = "192.168.1.1", isInputValid = true)
        assertEquals(expected, viewModel.uiState.first())
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `IPアドレスを変更するとUiStateが更新される`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val viewModel = buildViewModel()

        viewModel.onIpChanged("10.0.0.1")

        assertEquals(OtherServerIpDetailUiState(inputIp = "10.0.0.1", isInputValid = true), viewModel.uiState.first())
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `不正なIPアドレスを入力するとisInputValidがfalseになる`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val viewModel = buildViewModel()

        viewModel.onIpChanged("invalid")

        assertFalse(viewModel.uiState.first().isInputValid)
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `サーバーに到達可能な場合は保存されisSavedがtrueになる`() = runTest {
        every { repository.serverIp() } returns ipFlow
        coEvery { repository.saveServerIp(any()) } answers { ipFlow.update { firstArg() } }
        val viewModel = buildViewModel(reachable = true)

        viewModel.onIpChanged("10.0.0.2")
        viewModel.onSave()

        assertEquals("10.0.0.2", ipFlow.first())
        assertTrue(viewModel.uiState.first().isSaved)
        verify(exactly = 1) { repository.serverIp() }
        coVerify(exactly = 1) { repository.saveServerIp("10.0.0.2") }
        confirmVerified(repository)
    }

    @Test
    fun `不正なIPアドレスは保存されない`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val viewModel = buildViewModel(reachable = true)

        viewModel.onIpChanged("bad")
        viewModel.onSave()

        assertEquals("192.168.1.1", ipFlow.first())
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `サーバーに到達不可能な場合は警告が表示されisSavedはfalseのまま`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val vm = buildViewModel(reachable = false)

        vm.onIpChanged("10.0.0.1")
        vm.onSave()

        assertTrue(vm.uiState.first().connectivityWarning)
        assertFalse(vm.uiState.first().isSaved)
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `到達不可能でもonSaveAnywayで保存できる`() = runTest {
        every { repository.serverIp() } returns ipFlow
        coEvery { repository.saveServerIp(any()) } answers { ipFlow.update { firstArg() } }
        val vm = buildViewModel(reachable = false)

        vm.onIpChanged("10.0.0.1")
        vm.onSave()
        vm.onSaveAnyway()

        assertEquals("10.0.0.1", ipFlow.first())
        assertTrue(vm.uiState.first().isSaved)
        verify(exactly = 1) { repository.serverIp() }
        coVerify(exactly = 1) { repository.saveServerIp("10.0.0.1") }
        confirmVerified(repository)
    }

    @Test
    fun `onDismissで入力内容と警告がリセットされる`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val vm = buildViewModel(reachable = false)

        vm.onIpChanged("10.0.0.99")
        vm.onSave()
        vm.onDismiss()

        val state = vm.uiState.first()
        assertEquals("192.168.1.1", state.inputIp)
        assertFalse(state.connectivityWarning)
        assertFalse(state.isSaved)
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `保存済みIPがない場合は空文字を返す`() = runTest {
        every { repository.serverIp() } returns MutableStateFlow(null)
        val vm = buildViewModel()

        assertEquals(OtherServerIpDetailUiState(inputIp = "", isInputValid = true), vm.uiState.first())
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `255を超える値を持つIPアドレスは不正と判定される`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val viewModel = buildViewModel()

        viewModel.onIpChanged("256.0.0.1")

        assertFalse(viewModel.uiState.first().isInputValid)
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `境界値の255は有効と判定される`() = runTest {
        every { repository.serverIp() } returns ipFlow
        val viewModel = buildViewModel()

        viewModel.onIpChanged("255.255.255.255")

        assertTrue(viewModel.uiState.first().isInputValid)
        verify(exactly = 1) { repository.serverIp() }
        confirmVerified(repository)
    }

    @Test
    fun `保存に失敗するとsaveFailedがtrueになる`() = runTest {
        every { repository.serverIp() } returns ipFlow
        coEvery { repository.saveServerIp(any()) } throws IOException("保存失敗")
        val vm = buildViewModel(reachable = true)

        vm.onIpChanged("10.0.0.1")
        vm.onSave()

        assertTrue(vm.uiState.first().saveFailed)
        verify(exactly = 1) { repository.serverIp() }
        coVerify(exactly = 1) { repository.saveServerIp("10.0.0.1") }
        confirmVerified(repository)
    }

    @Test
    fun `保存失敗後に再度保存が成功するとsaveFailedがfalseにリセットされる`() = runTest {
        every { repository.serverIp() } returns ipFlow
        coEvery { repository.saveServerIp(any()) } throws IOException("保存失敗") andThenAnswer {
            ipFlow.update { firstArg() }
        }
        val vm = buildViewModel(reachable = true)

        vm.onIpChanged("10.0.0.1")
        vm.onSave()
        vm.onSave()

        assertFalse(vm.uiState.first().saveFailed)
        verify(exactly = 1) { repository.serverIp() }
        coVerify(exactly = 2) { repository.saveServerIp("10.0.0.1") }
        confirmVerified(repository)
    }
}
