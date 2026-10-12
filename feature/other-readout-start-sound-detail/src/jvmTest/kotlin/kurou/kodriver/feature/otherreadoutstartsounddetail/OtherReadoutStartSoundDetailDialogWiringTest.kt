package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class OtherReadoutStartSoundDetailDialogWiringTest {
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

    private val repository: ReadoutStartSoundPreferencesRepository = mockk()
    private val lmuEngine: TextToSpeechEngine = mockk()
    private val gt7Engine: TextToSpeechEngine = mockk()
    private val aceEngine: TextToSpeechEngine = mockk()

    @Test
    fun `同じViewModelStoreOwnerでシミュレータを切り替えて再表示すると対応するエンジンだけで試聴する`() {
        every { repository.observeType() } returns flowOf(ReadoutStartSoundType.FORMULA_RADIO)
        every { lmuEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) } returns Unit
        every { gt7Engine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) } returns Unit
        every { aceEngine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE) } returns Unit
        val engines =
            mapOf(
                Simulator.LmuWindows.id to lmuEngine,
                Simulator.Gt7Ps5.id to gt7Engine,
                Simulator.AceWindows.id to aceEngine,
            )
        val owner =
            object : ViewModelStoreOwner {
                override val viewModelStore = ViewModelStore()
            }
        var simulatorId by mutableStateOf(Simulator.LmuWindows.id)
        var showDialog by mutableStateOf(false)
        startKoin {
            modules(
                otherReadoutStartSoundDetailModule,
                module {
                    single<ReadoutStartSoundPreferencesRepository> { repository }
                    engines.forEach { (id, engine) ->
                        single<TextToSpeechEngine>(named(id)) { engine }
                    }
                },
            )
        }
        try {
            rule.setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    KoDriverTheme {
                        if (showDialog) {
                            OtherReadoutStartSoundDetailDialog(
                                simulatorId = simulatorId,
                                onDismiss = { showDialog = false },
                            )
                        }
                    }
                }
            }
            val previewedIds = mutableSetOf<String>()
            engines.keys.forEach { id ->
                rule.runOnIdle {
                    simulatorId = id
                    showDialog = true
                }
                rule.onNodeWithText("電子ノイズ").performClick()
                previewedIds.add(id)
                engines.forEach { (engineId, engine) ->
                    verify(exactly = if (engineId in previewedIds) 1 else 0) {
                        engine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE)
                    }
                }
                rule.onNodeWithText("キャンセル").performClick()
                rule.waitForIdle()
            }
            verify(exactly = 3) { repository.observeType() }
            confirmVerified(repository, lmuEngine, gt7Engine, aceEngine)
        } finally {
            rule.runOnIdle {
                showDialog = false
            }
            rule.waitForIdle()
            rule.runOnIdle { owner.viewModelStore.clear() }
            stopKoin()
        }
    }
}
