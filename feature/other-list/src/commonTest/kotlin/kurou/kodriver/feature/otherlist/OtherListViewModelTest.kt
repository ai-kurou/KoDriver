package kurou.kodriver.feature.otherlist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.model.AppUpdate
import kurou.kodriver.domain.repository.AppUpdateRepository
import kurou.kodriver.domain.repository.ExitConfirmationPreferencesRepository
import kurou.kodriver.domain.repository.KeepScreenOnPreferencesRepository
import kurou.kodriver.domain.usecase.CheckAppUpdateAvailableUseCase
import kurou.kodriver.domain.usecase.ObserveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.SaveExitConfirmationEnabledUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class FakeAppUpdateRepository(
    private val latestRelease: AppUpdate? = null,
) : AppUpdateRepository {
    override suspend fun getLatestRelease(): AppUpdate? = latestRelease
}

internal class FakeExitConfirmationPreferencesRepository(
    initial: Boolean = true,
) : ExitConfirmationPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun exitConfirmationEnabled(): Flow<Boolean> = flow

    override suspend fun saveExitConfirmationEnabled(enabled: Boolean) {
        flow.update { enabled }
    }

    fun updateExitConfirmationEnabled(enabled: Boolean) {
        flow.update { enabled }
    }
}

internal class FakeKeepScreenOnPreferencesRepository(
    initial: Boolean = true,
) : KeepScreenOnPreferencesRepository {
    private val flow = MutableStateFlow(initial)

    override fun keepScreenOn(): Flow<Boolean> = flow

    override suspend fun saveKeepScreenOn(enabled: Boolean) {
        flow.update { enabled }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        flow.update { enabled }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var keepScreenOnPreferencesRepository: FakeKeepScreenOnPreferencesRepository
    private lateinit var exitConfirmationPreferencesRepository: FakeExitConfirmationPreferencesRepository
    private lateinit var viewModel: OtherListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        keepScreenOnPreferencesRepository = FakeKeepScreenOnPreferencesRepository()
        exitConfirmationPreferencesRepository = FakeExitConfirmationPreferencesRepository()
        viewModel = OtherListViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository()),
            observeKeepScreenOn = ObserveKeepScreenOnUseCase(keepScreenOnPreferencesRepository),
            saveKeepScreenOn = SaveKeepScreenOnUseCase(keepScreenOnPreferencesRepository),
            observeExitConfirmationEnabled = ObserveExitConfirmationEnabledUseCase(
                exitConfirmationPreferencesRepository,
            ),
            saveExitConfirmationEnabled = SaveExitConfirmationEnabledUseCase(exitConfirmationPreferencesRepository),
            currentVersion = "0.5.0",
            appVersionLabel = "Windows版KoDriverバージョン",
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `初期状態では全項目が表示され選択項目はない`() = runTest {
        assertEquals(buildOtherListItems(), viewModel.uiState.first().items)
        assertEquals("Windows版KoDriverバージョン", viewModel.uiState.first().appVersionLabel)
        assertEquals("0.5.0", viewModel.uiState.first().appVersion)
        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `音量を選択すると選択状態になる`() = runTest {
        viewModel.onItemSelected(OtherListItemType.Volume)
        advanceMainUntilIdle()

        assertEquals(OtherListItemType.Volume, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `GitHubレポジトリを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.GitHubRepository)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `リリースページを選択しても状態は変わらない`() = runTest {
        val initialState = viewModel.uiState.first()

        viewModel.onItemSelected(OtherListItemType.ReleasePage)

        assertEquals(initialState, viewModel.uiState.first())
    }

    @Test
    fun `onItemSelectedで項目を選択し再選択すると解除される`() = runTest {
        viewModel.onItemSelected(OtherListItemType.License)
        advanceMainUntilIdle()

        assertEquals(OtherListItemType.License, viewModel.uiState.first().selectedItem)

        viewModel.onItemSelected(OtherListItemType.License)
        advanceMainUntilIdle()

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `selectItemで同じ項目を連続して選択しても選択状態が維持される`() = runTest {
        viewModel.selectItem(OtherListItemType.ConsoleIp)
        advanceMainUntilIdle()
        assertEquals(OtherListItemType.ConsoleIp, viewModel.uiState.first().selectedItem)

        viewModel.selectItem(OtherListItemType.ConsoleIp)
        advanceMainUntilIdle()
        assertEquals(OtherListItemType.ConsoleIp, viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `clearSelectedItemで選択状態が解除される`() = runTest {
        viewModel.onItemSelected(OtherListItemType.License)
        advanceMainUntilIdle()

        viewModel.clearSelectedItem()
        advanceMainUntilIdle()

        assertNull(viewModel.uiState.first().selectedItem)
    }

    @Test
    fun `終了確認の有効状態を監視できる`() = runTest {
        val repository = FakeExitConfirmationPreferencesRepository()
        val viewModel = OtherListViewModel(
            checkAppUpdateAvailable = CheckAppUpdateAvailableUseCase(FakeAppUpdateRepository()),
            observeKeepScreenOn = ObserveKeepScreenOnUseCase(keepScreenOnPreferencesRepository),
            saveKeepScreenOn = SaveKeepScreenOnUseCase(keepScreenOnPreferencesRepository),
            observeExitConfirmationEnabled = ObserveExitConfirmationEnabledUseCase(repository),
            saveExitConfirmationEnabled = SaveExitConfirmationEnabledUseCase(repository),
            currentVersion = "0.5.0",
            appVersionLabel = "Windows版KoDriverバージョン",
        )
        advanceMainUntilIdle()

        repository.updateExitConfirmationEnabled(false)
        advanceMainUntilIdle()

        assertEquals(false, viewModel.uiState.first().exitConfirmationEnabled)
    }

    @Test
    fun `onExitConfirmationEnabledChangeで終了確認の有効状態を保存できる`() = runTest {
        viewModel.onExitConfirmationEnabledChange(false)
        advanceMainUntilIdle()

        assertEquals(false, exitConfirmationPreferencesRepository.exitConfirmationEnabled().first())
        assertEquals(false, viewModel.uiState.first().exitConfirmationEnabled)
    }

    @Test
    fun `画面スリープ無効の状態を監視できる`() = runTest {
        keepScreenOnPreferencesRepository.updateKeepScreenOn(false)
        advanceMainUntilIdle()

        assertEquals(false, viewModel.uiState.first().keepScreenOn)
    }

    @Test
    fun `onKeepScreenOnChangeで画面スリープ無効の状態を保存できる`() = runTest {
        viewModel.onKeepScreenOnChange(false)
        advanceMainUntilIdle()

        assertEquals(false, keepScreenOnPreferencesRepository.keepScreenOn().first())
        assertEquals(false, viewModel.uiState.first().keepScreenOn)
    }

    private fun advanceMainUntilIdle() {
        dispatcher.scheduler.advanceUntilIdle()
    }
}
