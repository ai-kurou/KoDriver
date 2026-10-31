package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutRequest
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kurou.kodriver.domain.repository.ManualReadoutRequestRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ManualReadoutListPaneWiringTest {
    init {
        registerAllManualReadoutSimulators()
    }

    private val rule = createComposeRule()

    @get:Rule
    val rules: TestRule =
        RuleChain
            .outerRule(
                object : ExternalResource() {
                    override fun before() {
                        Dispatchers.setMain(UnconfinedTestDispatcher())
                    }

                    override fun after() {
                        Dispatchers.resetMain()
                    }
                },
            ).around(rule)

    private val requestRepository: ManualReadoutRequestRepository = mockk()
    private val lmuRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val gt7Repository: ManualReadoutSetPreferencesRepository = mockk()
    private val layoutRepository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository = mockk()

    @Test
    fun `Contentはシミュレーターごとの選択を保持して離脱後の再表示でも詳細を表示する`() {
        val sets = MutableStateFlow(listOf(ManualReadoutSet("lmu-set")) + List(39) { ManualReadoutSet("set-$it") })
        every { lmuRepository.observeSets() } returns sets
        every { layoutRepository.observeManualReadoutPlayButtonLayoutEnabled() } returns MutableStateFlow(false)
        coEvery { lmuRepository.deleteSet("lmu-set") } answers { sets.update { emptyList() } }
        every { gt7Repository.observeSets() } returns flowOf(listOf(ManualReadoutSet("gt7-set")))
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        var simulatorId by mutableStateOf(Simulator.LmuWindows.id)
        var showContent by mutableStateOf(true)
        var tabReselectRequest by mutableIntStateOf(0)
        startKoin {
            modules(
                manualReadoutListModule,
                module {
                    single<ManualReadoutRequestRepository> { requestRepository }
                    single<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository> { layoutRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) { lmuRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(Simulator.Gt7Ps5.id)) { gt7Repository }
                },
            )
        }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    KoDriverTheme {
                        Box(modifier = Modifier.requiredSize(840.dp, 640.dp)) {
                            if (showContent) {
                                ManualReadoutContent(
                                    simulatorId = simulatorId,
                                    tabReselectRequest = tabReselectRequest,
                                    simulatorName = simulatorId,
                                    detailContent = { id, _, _ -> Text("詳細 $id") },
                                )
                            }
                        }
                    }
                }
            }
            rule.onNodeWithText("LMU(Windows)セット1").assertIsDisplayed().performClick()
            rule.onNodeWithText("詳細 lmu-set").assertIsDisplayed()
            rule.runOnIdle { simulatorId = Simulator.Gt7Ps5.id }
            rule.waitForIdle()
            rule.onNodeWithText("詳細 lmu-set").assertDoesNotExist()
            rule.onNodeWithText("詳細 gt7-set").assertDoesNotExist()
            rule.onNodeWithText("GT7セット1").assertIsDisplayed()
            rule.runOnIdle { simulatorId = Simulator.LmuWindows.id }
            rule.waitForIdle()
            rule.onNodeWithText("詳細 lmu-set").assertIsDisplayed()
            rule.runOnIdle { showContent = false }
            rule.waitForIdle()
            rule.runOnIdle { showContent = true }
            rule.waitForIdle()
            rule.onNodeWithText("詳細 lmu-set").assertIsDisplayed()
            rule.runOnIdle { tabReselectRequest++ }
            rule.waitForIdle()
            rule.onNodeWithText("詳細 lmu-set").assertDoesNotExist()
            rule.onNode(hasScrollToIndexAction()).performScrollToIndex(25)
            rule.onNodeWithText("LMU(Windows)セット1").assertDoesNotExist()
            rule.runOnIdle { tabReselectRequest++ }
            rule.waitForIdle()
            rule.onNodeWithText("LMU(Windows)セット1").assertIsDisplayed().performClick()
            rule.onNodeWithText("詳細 lmu-set").assertIsDisplayed()
            rule.runOnIdle { showContent = false }
            rule.waitForIdle()
            rule.runOnIdle { showContent = true }
            rule.waitForIdle()
            rule.onNodeWithText("詳細 lmu-set").assertIsDisplayed()
            rule.onNodeWithText("LMU(Windows)セット1").performTouchInput { longClick() }
            rule.onNodeWithText("削除").performClick()
            rule.onNodeWithText("削除").performClick()
            rule.onNodeWithText("詳細 lmu-set").assertDoesNotExist()
            rule.onNodeWithText("lmu_windowsの手動読み上げセットはまだありません").assertIsDisplayed()
            verify(exactly = 1) { lmuRepository.observeSets() }
            coVerify(exactly = 1) { lmuRepository.deleteSet("lmu-set") }
            verify(exactly = 2) { layoutRepository.observeManualReadoutPlayButtonLayoutEnabled() }
            confirmVerified(lmuRepository, layoutRepository)
        } finally {
            rule.runOnIdle { showContent = false }
            rule.waitForIdle()
            rule.runOnIdle { owner.viewModelStore.clear() }
            stopKoin()
        }
    }

    @Test
    fun `同じViewModelStoreOwnerでシミュレーターを切り替えると対応するセットの空状態を表示する`() {
        val request = ManualReadoutRequest(Simulator.LmuWindows.id, "lmu-set")
        coEvery { requestRepository.request(request) } returns Unit
        val addedSet = slot<ManualReadoutSet>()
        coEvery { lmuRepository.addSet(capture(addedSet)) } returns Unit
        val lmuSets = MutableStateFlow<List<ManualReadoutSet>>(emptyList())
        every { lmuRepository.observeSets() } returns lmuSets
        coEvery { lmuRepository.deleteSet("lmu-set") } answers { lmuSets.update { emptyList() } }
        every { gt7Repository.observeSets() } returns flowOf(listOf(ManualReadoutSet(id = "gt7-set")))
        every { layoutRepository.observeManualReadoutPlayButtonLayoutEnabled() } returns MutableStateFlow(false)
        coEvery { layoutRepository.saveManualReadoutPlayButtonLayoutEnabled(true) } returns Unit
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        var simulatorId by mutableStateOf(Simulator.LmuWindows.id)
        var showPane by mutableStateOf(true)
        startKoin {
            modules(
                manualReadoutListModule,
                module {
                    single<ManualReadoutRequestRepository> { requestRepository }
                    single<ManualReadoutPlayButtonLayoutEnabledPreferencesRepository> { layoutRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) { lmuRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(Simulator.Gt7Ps5.id)) { gt7Repository }
                },
            )
        }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    KoDriverTheme {
                        if (showPane) {
                            ManualReadoutListPane(simulatorId = simulatorId, simulatorName = simulatorId)
                        }
                    }
                }
            }
            rule.onNodeWithText("lmu_windowsの手動読み上げセットはまだありません").assertIsDisplayed()
            rule.runOnIdle { lmuSets.update { listOf(ManualReadoutSet(id = "lmu-set")) } }
            rule.onNodeWithText("lmu_windowsの手動読み上げセットはまだありません").assertDoesNotExist()
            rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsEnabled().performClick()
            rule.runOnIdle { simulatorId = Simulator.Gt7Ps5.id }
            rule.waitForIdle()
            rule.onNodeWithContentDescription("セット 1 を読み上げ").assertIsEnabled()
            rule.onNodeWithText("手動読み上げについて").assertIsDisplayed()
            rule.onNodeWithContentDescription("標準表示").performClick()
            coVerify(exactly = 0) { layoutRepository.saveManualReadoutPlayButtonLayoutEnabled(true) }
            rule.onNodeWithContentDescription("再生ボタン表示").performClick()
            coVerify(exactly = 1) { layoutRepository.saveManualReadoutPlayButtonLayoutEnabled(true) }
            rule.onNodeWithText("gt7_ps5の手動読み上げセットはまだありません").assertDoesNotExist()
            rule.runOnIdle { simulatorId = Simulator.LmuWindows.id }
            rule.onNodeWithText("LMU(Windows)セット1").performTouchInput { longClick() }
            rule.onNodeWithText("削除").performClick()
            rule.onNodeWithText("削除").performClick()
            rule.onNodeWithText("lmu_windowsの手動読み上げセットはまだありません").assertIsDisplayed()
            rule.onNodeWithContentDescription("セットを追加").assertIsDisplayed().performClick()
            rule.runOnIdle {
                val saved = addedSet.captured
                assertEquals(emptyList(), saved.itemKeys)
                assertEquals(
                    saved.id,
                    java.util.UUID
                        .fromString(saved.id)
                        .toString(),
                )
                coVerify(exactly = 1) { lmuRepository.addSet(saved) }
            }
            verify(exactly = 1) { lmuRepository.observeSets() }
            verify(exactly = 1) { gt7Repository.observeSets() }
            coVerify(exactly = 1) { requestRepository.request(request) }
            coVerify(exactly = 1) { lmuRepository.deleteSet("lmu-set") }
            verify(exactly = 2) { layoutRepository.observeManualReadoutPlayButtonLayoutEnabled() }
            confirmVerified(lmuRepository, gt7Repository, requestRepository, layoutRepository)
        } finally {
            rule.runOnIdle { showPane = false }
            rule.waitForIdle()
            rule.runOnIdle { owner.viewModelStore.clear() }
            stopKoin()
        }
    }
}
