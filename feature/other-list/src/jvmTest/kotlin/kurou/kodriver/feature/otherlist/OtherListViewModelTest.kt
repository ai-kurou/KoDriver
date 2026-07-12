package kurou.kodriver.feature.otherlist

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
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ExitConfirmationEnabledRepository
import kurou.kodriver.domain.repository.KeepScreenOnEnabledRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnEnabledUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnEnabledUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var appUpdateRepository: AppUpdateRepository

    @MockK
    private lateinit var keepScreenOnRepository: KeepScreenOnEnabledRepository

    @MockK
    private lateinit var exitConfirmationRepository: ExitConfirmationEnabledRepository

    private val keepScreenOnFlow = MutableStateFlow(true)
    private val exitConfirmationFlow = MutableStateFlow(true)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(currentVersion: String = "0.5.0") = OtherListViewModel(
        checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(appUpdateRepository),
        observeKeepScreenOn = ObserveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
        saveKeepScreenOn = SaveKeepScreenOnEnabledUseCase(keepScreenOnRepository),
        observeExitConfirmationEnabled = ObserveExitConfirmationEnabledUseCase(exitConfirmationRepository),
        saveExitConfirmationEnabled = SaveExitConfirmationEnabledUseCase(exitConfirmationRepository),
        currentVersion = currentVersion,
        appVersionLabel = "Windows版KoDriverバージョン",
    )

    @Test
    fun `初期状態では全項目が表示され選択項目はない`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        assertEquals(buildOtherListItems(), viewModel.uiState.first().items)
        assertEquals("Windows版KoDriverバージョン", viewModel.uiState.first().appVersionLabel)
        assertEquals("0.5.0", viewModel.uiState.first().appVersion)
        assertNull(viewModel.uiState.first().selectedItem)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `音量を選択すると選択状態になる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        viewModel.onItemSelected(OtherListItemType.Volume)

        assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `GitHubレポジトリを選択しても状態は変わらない`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.GitHubRepository)

        assertEquals(initialState, viewModel.uiState.first())
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `リリースページを選択しても状態は変わらない`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.ReleasePage)

        assertEquals(initialState, viewModel.uiState.first())
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `onItemSelectedで項目を選択し再選択すると解除される`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        viewModel.onItemSelected(OtherListItemType.License)
        assertEquals(OtherListItemType.License, viewModel.uiState.first().selectedItem)

        viewModel.onItemSelected(OtherListItemType.License)
        assertNull(viewModel.uiState.first().selectedItem)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `selectItemで同じ項目を連続して選択しても選択状態が維持される`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        viewModel.selectItem(OtherListItemType.ConsoleIp)
        assertEquals(OtherListItemType.ConsoleIp, viewModel.uiState.first().selectedItem)

        viewModel.selectItem(OtherListItemType.ConsoleIp)
        assertEquals(OtherListItemType.ConsoleIp, viewModel.uiState.first().selectedItem)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        viewModel.onItemSelected(OtherListItemType.License)
        viewModel.clearSelectedItem()

        assertNull(viewModel.uiState.first().selectedItem)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `終了確認の有効状態を監視できる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        exitConfirmationFlow.update { false }

        assertEquals(false, viewModel.uiState.first().exitConfirmationEnabled)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `onExitConfirmationEnabledChangeで終了確認の有効状態を保存できる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        viewModel.onExitConfirmationEnabledChange(false)

        assertEquals(false, exitConfirmationFlow.first())
        assertEquals(false, viewModel.uiState.first().exitConfirmationEnabled)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        coVerify(exactly = 1) { exitConfirmationRepository.saveExitConfirmationEnabled(false) }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `画面スリープ無効の状態を監視できる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        keepScreenOnFlow.update { false }

        assertEquals(false, viewModel.uiState.first().keepScreenOn)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `onKeepScreenOnChangeで画面スリープ無効の状態を保存できる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel()

        viewModel.onKeepScreenOnChange(false)

        assertEquals(false, keepScreenOnFlow.first())
        assertEquals(false, viewModel.uiState.first().keepScreenOn)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        coVerify(exactly = 1) { keepScreenOnRepository.saveKeepScreenOn(false) }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `最新バージョンがある場合hasAppUpdateがtrueになる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        coEvery { appUpdateRepository.getLatestRelease() } returns AppUpdate(tagName = "v9.9.9")
        val viewModel = createViewModel(currentVersion = "1.0.0")

        viewModel.checkUpdate()

        assertTrue(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `現在が最新バージョンの場合hasAppUpdateがfalseになる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        coEvery { appUpdateRepository.getLatestRelease() } returns AppUpdate(tagName = "v1.0.0")
        val viewModel = createViewModel(currentVersion = "1.0.0")

        viewModel.checkUpdate()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `checkUpdateを呼ぶ前はhasAppUpdateがfalseのまま`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel(currentVersion = "1.0.0")

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `currentVersionが空の場合checkUpdateは何もしない`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        val viewModel = createViewModel(currentVersion = "")

        viewModel.checkUpdate()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }

    @Test
    fun `リリース情報がnullの場合hasAppUpdateがfalseになる`() = runTest {
        every { keepScreenOnRepository.keepScreenOn() } returns keepScreenOnFlow
        coEvery { keepScreenOnRepository.saveKeepScreenOn(any()) } answers { keepScreenOnFlow.update { firstArg() } }
        every { exitConfirmationRepository.exitConfirmationEnabled() } returns exitConfirmationFlow
        coEvery { exitConfirmationRepository.saveExitConfirmationEnabled(any()) } answers {
            exitConfirmationFlow.update { firstArg() }
        }
        coEvery { appUpdateRepository.getLatestRelease() } returns null
        val viewModel = createViewModel(currentVersion = "1.0.0")

        viewModel.checkUpdate()

        assertFalse(viewModel.uiState.first().hasAppUpdate)
        coVerify(exactly = 1) { appUpdateRepository.getLatestRelease() }
        verify(exactly = 1) { keepScreenOnRepository.keepScreenOn() }
        verify(exactly = 1) { exitConfirmationRepository.exitConfirmationEnabled() }
        confirmVerified(appUpdateRepository, keepScreenOnRepository, exitConfirmationRepository)
    }
}
