package kurou.kodriver.feature.othervoicespeeddetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVoiceSpeedDetailFlowTest {
    private val speed = MutableStateFlow(1.5f)
    private val isPreviewing = MutableStateFlow(false)

    @Test
    fun `保存済み速度と試聴状態の更新をそのまま反映する`() =
        runTest {
            val flow = OtherVoiceSpeedDetailFlow(speed = speed, isPreviewing = isPreviewing)
            val states = mutableListOf<OtherVoiceSpeedDetailUiState>()
            val subscription = flow.uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()

            speed.update { 0.70000005f }
            runCurrent()
            isPreviewing.update { true }
            runCurrent()
            speed.update { 2.0f }
            runCurrent()
            isPreviewing.update { false }
            runCurrent()

            assertEquals(
                listOf(
                    OtherVoiceSpeedDetailUiState(speed = 1.5f),
                    OtherVoiceSpeedDetailUiState(speed = 0.70000005f),
                    OtherVoiceSpeedDetailUiState(speed = 0.70000005f, isPreviewing = true),
                    OtherVoiceSpeedDetailUiState(speed = 2.0f, isPreviewing = true),
                    OtherVoiceSpeedDetailUiState(speed = 2.0f),
                ),
                states,
            )
            subscription.cancel()
        }

    @Test
    fun `購読するまで入力を収集せず購読ごとに収集する`() =
        runTest {
            var collections = 0
            val observedSpeed =
                flow {
                    collections++
                    emit(1.2f)
                }
            val flow = OtherVoiceSpeedDetailFlow(speed = observedSpeed, isPreviewing = isPreviewing)

            assertEquals(0, collections)
            assertEquals(OtherVoiceSpeedDetailUiState(speed = 1.2f), flow.uiState.first())
            assertEquals(1, collections)
            assertEquals(OtherVoiceSpeedDetailUiState(speed = 1.2f), flow.uiState.first())
            assertEquals(2, collections)
        }
}
