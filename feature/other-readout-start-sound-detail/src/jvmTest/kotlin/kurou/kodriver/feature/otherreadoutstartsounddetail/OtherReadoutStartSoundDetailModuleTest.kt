package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.lifecycle.viewModelScope
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.engine.TextToSpeechEngine
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ReadoutStartSoundPreferencesRepository
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OtherReadoutStartSoundDetailModuleTest {
    private val repository: ReadoutStartSoundPreferencesRepository = mockk()
    private val lmuEngine: TextToSpeechEngine = mockk()
    private val gt7Engine: TextToSpeechEngine = mockk()
    private val aceEngine: TextToSpeechEngine = mockk()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `各シミュレータのパラメータで解決したViewModelは対応するエンジンだけで試聴する`() {
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
        val koin =
            koinApplication {
                modules(
                    otherReadoutStartSoundDetailModule,
                    module {
                        single<ReadoutStartSoundPreferencesRepository> { repository }
                        engines.forEach { (id, engine) ->
                            single<TextToSpeechEngine>(named(id)) { engine }
                        }
                    },
                )
            }.koin
        try {
            val previewedIds = mutableSetOf<String>()
            Simulator.entries.forEach { simulator ->
                val viewModel =
                    koin.get<OtherReadoutStartSoundDetailViewModel> {
                        parametersOf(simulator.id)
                    }
                try {
                    viewModel.onPendingTypeSelected(ReadoutStartSoundType.ELECTRONIC_NOISE)
                    previewedIds.add(simulator.id)
                    engines.forEach { (id, engine) ->
                        verify(exactly = if (id in previewedIds) 1 else 0) {
                            engine.previewStartSound(ReadoutStartSoundType.ELECTRONIC_NOISE)
                        }
                    }
                } finally {
                    viewModel.viewModelScope.cancel()
                }
            }
            verify(exactly = 3) { repository.observeType() }
            confirmVerified(repository, lmuEngine, gt7Engine, aceEngine)
        } finally {
            koin.close()
        }
    }
}
