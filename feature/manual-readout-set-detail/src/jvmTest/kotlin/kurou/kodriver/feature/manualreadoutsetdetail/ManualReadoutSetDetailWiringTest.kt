package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.manualReadoutItemKeys
import kurou.kodriver.domain.model.manualReadoutItemTextDefault
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
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
class ManualReadoutSetDetailWiringTest {
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

    private val debugRepository: ReadoutHotkeyPreferencesRepository = mockk()
    private val repository: ManualReadoutSetPreferencesRepository = mockk()
    private val otherRepository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `Contentは項目保存と削除と一覧へ戻る通知を配線する`() {
        val key = manualReadoutItemKeys(Simulator.LmuWindows.id).single()
        val original = ManualReadoutSet("target")
        val selected = original.copy(itemKeys = listOf(key))
        val edited = selected.copy(itemTexts = mapOf(key to "残量{percent}%"))
        val reset = edited.copy(itemTexts = mapOf(key to manualReadoutItemTextDefault(key)))
        val sets = MutableStateFlow(listOf(original))
        every { repository.observeSets() } returns sets
        every { debugRepository.observeVirtualKeyCode() } returns MutableStateFlow(0x70)
        coEvery { repository.updateSet(selected) } answers { sets.value = listOf(selected) }
        coEvery { repository.updateSet(edited) } answers { sets.value = listOf(edited) }
        coEvery { repository.updateSet(reset) } answers { sets.value = listOf(reset) }
        coEvery { repository.deleteSet("target") } returns Unit
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        var returned = 0
        var backed = 0
        startKoin {
            modules(
                manualReadoutSetDetailModule,
                module {
                    single<ReadoutHotkeyPreferencesRepository> { debugRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(Simulator.LmuWindows.id)) { repository }
                    Simulator.entries.filter { it != Simulator.LmuWindows }.forEach { simulator ->
                        single<ManualReadoutSetPreferencesRepository>(named(simulator.id)) { otherRepository }
                    }
                },
            )
        }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    KoDriverTheme {
                        ManualReadoutSetDetailContent(
                            simulatorId = Simulator.LmuWindows.id,
                            setId = "target",
                            onDeleted = { returned++ },
                            canNavigateBack = true,
                            onBack = { backed++ },
                            showHotkey = false,
                        )
                    }
                }
            }
            rule.onNodeWithText("セット 1").assertIsDisplayed()
            rule.onNodeWithContentDescription("戻る").performClick()
            rule.runOnIdle { assertEquals(1, backed) }
            rule.onNode(isToggleable()).performClick()
            rule.onNode(hasSetTextAction()).performTextReplacement("残量{percent}%")
            rule.waitForIdle()
            rule.onNodeWithText("残量{percent}%").assertIsDisplayed()
            rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
            rule.waitForIdle()
            rule.onNodeWithText(manualReadoutItemTextDefault(key)).assertIsDisplayed()
            rule.onNodeWithText("セットを削除").performClick()
            rule.onNodeWithText("削除").performClick()
            rule.waitForIdle()
            rule.runOnIdle { assertEquals(1, returned) }
            verify(exactly = 4) { repository.observeSets() }
            coVerify(exactly = 1) { repository.updateSet(selected) }
            coVerify(exactly = 1) { repository.updateSet(edited) }
            coVerify(exactly = 1) { repository.updateSet(reset) }
            coVerify(exactly = 1) { repository.deleteSet("target") }
            confirmVerified(repository)
        } finally {
            owner.viewModelStore.clear()
            stopKoin()
        }
    }

    @Test
    fun `Contentはホットキーの保存と解除を配線する`() {
        val original = ManualReadoutSet("target")
        val assigned = original.copy(hotkeyVirtualKeyCode = 0x41)
        val sets = MutableStateFlow(listOf(original))
        every { repository.observeSets() } returns sets
        every { debugRepository.observeVirtualKeyCode() } returns MutableStateFlow(0x70)
        every { otherRepository.observeSets() } returns MutableStateFlow(emptyList())
        coEvery { repository.updateSet(assigned) } answers { sets.value = listOf(assigned) }
        coEvery { repository.updateSet(original) } answers { sets.value = listOf(original) }
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        startKoin {
            modules(
                manualReadoutSetDetailModule,
                module {
                    single<ReadoutHotkeyPreferencesRepository> { debugRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(Simulator.Gt7Ps5.id)) { repository }
                    Simulator.entries.filter { it != Simulator.Gt7Ps5 }.forEach { simulator ->
                        single<ManualReadoutSetPreferencesRepository>(named(simulator.id)) { otherRepository }
                    }
                },
            )
        }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    KoDriverTheme {
                        ManualReadoutSetDetailContent(
                            simulatorId = Simulator.Gt7Ps5.id,
                            setId = "target",
                            onDeleted = {},
                            canNavigateBack = false,
                            onBack = {},
                        )
                    }
                }
            }
            rule.onNodeWithTag("manual_readout_hotkey_key").performClick().performKeyInput { pressKey(Key.A) }
            rule.onNodeWithText("Ctrl + Alt + Shift + A").assertIsDisplayed()
            rule.onNodeWithText("割り当てを解除").performClick()
            rule.onNodeWithText("割り当てなし").assertIsDisplayed()
            verify(exactly = 3) { repository.observeSets() }
            verify(exactly = 2) { debugRepository.observeVirtualKeyCode() }
            verify(exactly = 2) { otherRepository.observeSets() }
            coVerify(exactly = 1) { repository.updateSet(assigned) }
            coVerify(exactly = 1) { repository.updateSet(original) }
            confirmVerified(repository, debugRepository, otherRepository)
        } finally {
            owner.viewModelStore.clear()
            stopKoin()
        }
    }
}
