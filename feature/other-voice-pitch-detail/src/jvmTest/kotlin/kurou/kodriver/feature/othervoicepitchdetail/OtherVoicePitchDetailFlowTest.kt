package kurou.kodriver.feature.othervoicepitchdetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherVoicePitchDetailFlowTest {
    private val pitch = MutableStateFlow(1.0f)
    private val isPreviewing = MutableStateFlow(false)

    private fun createFlow() =
        OtherVoicePitchDetailFlow(
            pitch = pitch,
            isPreviewing = isPreviewing,
        )

    @Test
    fun `声の高さを丸めず試聴状態とともに反映する`() =
        runTest {
            val flow = createFlow()
            for (value in listOf(0.5f, 1.0f, 2.0f, 0.70000005f)) {
                for (previewing in listOf(false, true)) {
                    pitch.update { value }
                    isPreviewing.update { previewing }
                    assertEquals(
                        OtherVoicePitchDetailUiState(pitch = value, isPreviewing = previewing),
                        flow.uiState.first(),
                    )
                }
            }
        }

    @Test
    fun `声の高さと試聴開始と停止の更新を反映する`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherVoicePitchDetailUiState(), flow.uiState.first())

            isPreviewing.update { true }
            assertEquals(OtherVoicePitchDetailUiState(isPreviewing = true), flow.uiState.first())

            pitch.update { 1.5f }
            assertEquals(OtherVoicePitchDetailUiState(pitch = 1.5f, isPreviewing = true), flow.uiState.first())

            isPreviewing.update { false }
            assertEquals(OtherVoicePitchDetailUiState(pitch = 1.5f), flow.uiState.first())
        }

    @Test
    fun `購読するまで入力を収集せず購読ごとに収集する`() =
        runTest {
            var collections = 0
            val flow =
                OtherVoicePitchDetailFlow(
                    pitch =
                        flow {
                            collections++
                            emit(1.5f)
                        },
                    isPreviewing = isPreviewing,
                )

            assertEquals(0, collections)
            repeat(2) { index ->
                assertEquals(OtherVoicePitchDetailUiState(pitch = 1.5f), flow.uiState.first())
                assertEquals(index + 1, collections)
            }
        }
}
