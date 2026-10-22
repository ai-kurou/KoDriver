package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.ManualReadoutSet
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.registerAceWindowsReadoutItemKeys
import kurou.kodriver.domain.model.registerGt7Ps5ReadoutItemKeys
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import kurou.kodriver.domain.repository.ManualReadoutHotkeyRepository
import kurou.kodriver.domain.repository.ManualReadoutSetPreferencesRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.repository.TextToSpeechRepository
import kurou.kodriver.domain.usecase.PlayStartSoundForKeyUseCase
import kurou.kodriver.domain.usecase.SpeakTextUseCase
import org.junit.Before
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
    @Before
    fun registerReadoutKeys() {
        registerLmuWindowsReadoutItemKeys()
        registerGt7Ps5ReadoutItemKeys()
        registerAceWindowsReadoutItemKeys()
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

    private val ttsRepository: TextToSpeechRepository = mockk()
    private val volumeRepository: SoundVolumePreferencesRepository = mockk()
    private val playStartSound: PlayStartSoundForKeyUseCase = mockk()
    private val speakText: SpeakTextUseCase = mockk()
    private val hotkeyRepository: ManualReadoutHotkeyRepository = mockk()
    private val debugRepository: ReadoutHotkeyPreferencesRepository = mockk()
    private val repository: ManualReadoutSetPreferencesRepository = mockk()
    private val otherRepository: ManualReadoutSetPreferencesRepository = mockk()

    @Test
    fun `Contentは項目保存と削除と一覧へ戻る通知を配線する`() {
        assertItemWiring(Simulator.LmuWindows, "バーチャルエネルギー残量")
    }

    @Test
    fun `ACEのContentは残り燃料の追加と文言編集と試聴と削除を配線する`() {
        assertItemWiring(Simulator.AceWindows, "残り燃料")
    }

    @Test
    fun `GT7のContentは残り燃料の追加と文言編集と試聴と削除を配線する`() {
        assertItemWiring(Simulator.Gt7Ps5, "残り燃料")
    }

    private fun renameSet(name: String) {
        rule.onNodeWithContentDescription("セット名を編集").performClick()
        rule.onNodeWithTag("manual_readout_set_name_input").performTextReplacement(name)
        rule.onNodeWithContentDescription("確定").performClick()
        rule.waitForIdle()
        rule.onAllNodesWithText(name)[0].assertIsDisplayed()
    }

    private fun assertItemWiring(
        targetSimulator: Simulator,
        itemName: String,
    ) {
        coEvery { ttsRepository.isAvailable() } returns true
        every { hotkeyRepository.registrationStateStream(targetSimulator.id, "target") } returns
            MutableStateFlow(HotkeyRegistrationState.Unsupported)
        val key = manualReadoutItemKeys(targetSimulator.id).first()
        every { volumeRepository.volume() } returns MutableStateFlow(60)
        coEvery { playStartSound(ReadoutItemKey.fromValue(key)!!) } returns Unit
        coEvery { speakText("残量50%", volume = 60) } returns Unit
        val original = ManualReadoutSet("target")
        val selected = original.copy(itemKeys = listOf(key))
        val edited = selected.copy(itemTexts = mapOf(key to "残量{percent}%"))
        val reset = edited.copy(itemTexts = mapOf(key to manualReadoutItemTextDefault(targetSimulator.id, key)))
        val removed = reset.copy(itemKeys = emptyList())
        val named = removed.copy(name = "レース用")
        val sets = MutableStateFlow(listOf(original))
        every { repository.observeSets() } returns sets
        every { debugRepository.observeVirtualKeyCode() } returns MutableStateFlow(0x70)
        coEvery { repository.updateSet(selected) } answers { sets.value = listOf(selected) }
        coEvery { repository.updateSet(edited) } answers { sets.value = listOf(edited) }
        coEvery { repository.updateSet(reset) } answers { sets.value = listOf(reset) }
        coEvery { repository.updateSet(removed) } answers { sets.value = listOf(removed) }
        coEvery { repository.updateSet(named) } answers { sets.value = listOf(named) }
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
                    single<TextToSpeechRepository> { ttsRepository }
                    single<SoundVolumePreferencesRepository> { volumeRepository }
                    Simulator.entries.forEach { simulator ->
                        single<PlayStartSoundForKeyUseCase>(named(simulator.id)) { playStartSound }
                        single<SpeakTextUseCase>(named(simulator.id)) { speakText }
                    }
                    single<ManualReadoutHotkeyRepository> { hotkeyRepository }
                    single<ReadoutHotkeyPreferencesRepository> { debugRepository }
                    single<ManualReadoutSetPreferencesRepository>(named(targetSimulator.id)) { repository }
                    Simulator.entries.filter { it != targetSimulator }.forEach { simulator ->
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
                            simulatorId = targetSimulator.id,
                            setId = "target",
                            onDeleted = { returned++ },
                            canNavigateBack = true,
                            onBack = { backed++ },
                            showHotkey = false,
                        )
                    }
                }
            }
            rule.onAllNodesWithText("セット 1")[0].assertIsDisplayed()
            rule.onNodeWithContentDescription("戻る").performClick()
            rule.runOnIdle { assertEquals(1, backed) }
            rule.onNodeWithText("＋ 項目を追加").performClick()
            rule.onNodeWithText(itemName).performClick()
            rule.onNode(hasSetTextAction()).performTextReplacement("残量{percent}%")
            rule.waitForIdle()
            rule.onNodeWithText("残量{percent}%").assertIsDisplayed()
            rule.onNodeWithText("試聴").performClick()
            rule.waitForIdle()
            rule.onNodeWithContentDescription("文言をデフォルトに戻す").performClick()
            rule.waitForIdle()
            rule.onNodeWithText(manualReadoutItemTextDefault(targetSimulator.id, key)).assertIsDisplayed()
            rule.onNodeWithContentDescription("${itemName}を削除").performClick()
            rule.onNode(hasSetTextAction()).assertDoesNotExist()
            renameSet("レース用")
            rule.onNodeWithContentDescription("セットを削除").performClick()
            rule.onNodeWithText("削除").performClick()
            rule.waitForIdle()
            rule.runOnIdle { assertEquals(1, returned) }
            verify(exactly = 6) { repository.observeSets() }
            coVerify(exactly = 1) { repository.updateSet(selected) }
            coVerify(exactly = 1) { repository.updateSet(edited) }
            coVerify(exactly = 1) { repository.updateSet(reset) }
            coVerify(exactly = 1) { repository.updateSet(removed) }
            coVerify(exactly = 1) { repository.updateSet(named) }
            coVerify(exactly = 1) { repository.deleteSet("target") }
            verify(exactly = 1) { volumeRepository.volume() }
            coVerify(exactly = 1) { playStartSound(ReadoutItemKey.fromValue(key)!!) }
            coVerify(exactly = 1) { speakText("残量50%", volume = 60) }
            confirmVerified(repository, volumeRepository, playStartSound, speakText)
        } finally {
            owner.viewModelStore.clear()
            stopKoin()
        }
    }

    @Test
    fun `Contentはホットキーの保存と解除を配線する`() {
        coEvery { ttsRepository.isAvailable() } returns true
        val registration = MutableStateFlow<HotkeyRegistrationState>(HotkeyRegistrationState.Conflict)
        every { hotkeyRepository.registrationStateStream(Simulator.Gt7Ps5.id, "target") } returns registration
        coEvery { hotkeyRepository.retryRegistration(Simulator.Gt7Ps5.id, "target") } answers {
            registration.value = HotkeyRegistrationState.Registered
        }
        val original = ManualReadoutSet("target")
        val modified = original.copy(hotkeyModifiers = ManualReadoutHotkeyModifiers.CTRL_ALT)
        val assigned = modified.copy(hotkeyVirtualKeyCode = 0x41)
        val sets = MutableStateFlow(listOf(original))
        every { repository.observeSets() } returns sets
        every { debugRepository.observeVirtualKeyCode() } returns MutableStateFlow(0x70)
        every { otherRepository.observeSets() } returns MutableStateFlow(emptyList())
        coEvery { repository.updateSet(assigned) } answers { sets.value = listOf(assigned) }
        coEvery { repository.updateSet(modified) } answers { sets.value = listOf(modified) }
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        startKoin {
            modules(
                manualReadoutSetDetailModule,
                module {
                    single<TextToSpeechRepository> { ttsRepository }
                    single<SoundVolumePreferencesRepository> { volumeRepository }
                    Simulator.entries.forEach { simulator ->
                        single<PlayStartSoundForKeyUseCase>(named(simulator.id)) { playStartSound }
                        single<SpeakTextUseCase>(named(simulator.id)) { speakText }
                    }
                    single<ManualReadoutHotkeyRepository> { hotkeyRepository }
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
                            showHotkey = true,
                        )
                    }
                }
            }
            rule.onNodeWithTag("manual_readout_hotkey_modifiers").performClick()
            rule.onNodeWithTag("manual_readout_hotkey_modifiers_CTRL_ALT").performClick()
            rule.onNodeWithText("Ctrl + Alt").assertIsDisplayed()
            rule.onNodeWithTag("manual_readout_hotkey_key").performClick().performKeyInput { pressKey(Key.A) }
            rule.onNodeWithText("A").assertIsDisplayed()
            rule.onNodeWithText("他のアプリが使用中のため登録できません").assertIsDisplayed()
            rule.onNodeWithText("再試行").performClick()
            rule.onNodeWithText("登録済み").assertIsDisplayed()
            rule.onNodeWithText("割り当てを解除").performClick()
            rule.onNodeWithText("割り当てなし").assertIsDisplayed()
            verify(exactly = 4) { repository.observeSets() }
            verify(exactly = 1) { debugRepository.observeVirtualKeyCode() }
            verify(exactly = 2) { otherRepository.observeSets() }
            coVerify(exactly = 1) { repository.updateSet(assigned) }
            coVerify(exactly = 2) { repository.updateSet(modified) }
            verify(exactly = 1) { hotkeyRepository.registrationStateStream(Simulator.Gt7Ps5.id, "target") }
            coVerify(exactly = 1) { hotkeyRepository.retryRegistration(Simulator.Gt7Ps5.id, "target") }
            confirmVerified(repository, debugRepository, otherRepository, hotkeyRepository)
        } finally {
            owner.viewModelStore.clear()
            stopKoin()
        }
    }
}
