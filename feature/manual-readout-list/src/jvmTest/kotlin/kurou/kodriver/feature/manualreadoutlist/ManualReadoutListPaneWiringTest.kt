package kurou.kodriver.feature.manualreadoutlist

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
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

    private val lmuRepository: ManualReadoutSetPreferencesRepository = mockk()
    private val gt7Repository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `同じViewModelStoreOwnerでシミュレーターを切り替えると対応するセットの空状態を表示する`() {
        val savedSets = slot<List<ManualReadoutSet>>()
        coEvery { lmuRepository.saveSets(capture(savedSets)) } returns Unit
        val lmuSets = MutableStateFlow<List<ManualReadoutSet>>(emptyList())
        every { lmuRepository.observeSets() } returns lmuSets
        every { gt7Repository.observeSets() } returns flowOf(listOf(ManualReadoutSet(id = "gt7-set")))
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
            rule.runOnIdle { simulatorId = Simulator.Gt7Ps5.id }
            rule.waitForIdle()
            rule.onNodeWithText("手動読み上げについて").assertIsDisplayed()
            rule.onNodeWithText("gt7_ps5の手動読み上げセットはまだありません").assertDoesNotExist()
            rule.runOnIdle {
                lmuSets.update { emptyList() }
                simulatorId = Simulator.LmuWindows.id
            }
            rule.onNodeWithText("lmu_windowsの手動読み上げセットはまだありません").assertIsDisplayed()
            rule.onNodeWithText("セットを追加", useUnmergedTree = true).assertIsDisplayed().performClick()
            rule.runOnIdle {
                val saved = savedSets.captured
                assertEquals(1, saved.size)
                assertEquals(emptyList(), saved.single().itemKeys)
                assertEquals(
                    saved.single().id,
                    java.util.UUID
                        .fromString(saved.single().id)
                        .toString(),
                )
                coVerify(exactly = 1) { lmuRepository.saveSets(saved) }
            }
            verify(exactly = 1) { lmuRepository.observeSets() }
            verify(exactly = 1) { gt7Repository.observeSets() }
            confirmVerified(lmuRepository, gt7Repository)
        } finally {
            rule.runOnIdle { showPane = false }
            rule.waitForIdle()
            rule.runOnIdle { owner.viewModelStore.clear() }
            stopKoin()
        }
    }
}
