package kurou.kodriver.feature.otherandroidaudiousagedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherAndroidAudioUsageDetailFlowTest {
    @Test
    fun `保存済み用途と試聴中用途の更新と試聴停止を反映する`() =
        runTest {
            val usage = MutableStateFlow(AndroidAudioUsage.MEDIA)
            val previewingUsage = MutableStateFlow<AndroidAudioUsage?>(null)
            val flow = OtherAndroidAudioUsageDetailFlow(usage, previewingUsage)
            val states = mutableListOf<OtherAndroidAudioUsageDetailUiState>()
            flow.uiState.onEach { states.add(it) }.launchIn(backgroundScope)
            runCurrent()
            assertEquals(OtherAndroidAudioUsageDetailUiState(), states.last())

            for (selected in AndroidAudioUsage.entries) {
                usage.update { selected }
                runCurrent()
                assertEquals(OtherAndroidAudioUsageDetailUiState(usage = selected), states.last())

                for (preview in AndroidAudioUsage.entries) {
                    previewingUsage.update { preview }
                    runCurrent()
                    assertEquals(
                        OtherAndroidAudioUsageDetailUiState(usage = selected, previewingUsage = preview),
                        states.last(),
                    )
                }
                previewingUsage.update { null }
                runCurrent()
                assertEquals(OtherAndroidAudioUsageDetailUiState(usage = selected), states.last())
            }

            previewingUsage.update { AndroidAudioUsage.ALARM }
            runCurrent()
            usage.update { AndroidAudioUsage.MEDIA }
            runCurrent()
            assertEquals(
                OtherAndroidAudioUsageDetailUiState(
                    usage = AndroidAudioUsage.MEDIA,
                    previewingUsage = AndroidAudioUsage.ALARM,
                ),
                states.last(),
            )
        }

    @Test
    fun `購読するまで上流を収集せず購読ごとに合成する`() =
        runTest {
            var usageCollections = 0
            var previewCollections = 0
            val flow =
                OtherAndroidAudioUsageDetailFlow(
                    usage =
                        flow {
                            usageCollections++
                            emit(AndroidAudioUsage.COMMUNICATION)
                        },
                    previewingUsage =
                        flow {
                            previewCollections++
                            emit(AndroidAudioUsage.ALARM)
                        },
                )
            assertEquals(0, usageCollections)
            assertEquals(0, previewCollections)

            repeat(2) { index ->
                assertEquals(
                    OtherAndroidAudioUsageDetailUiState(
                        usage = AndroidAudioUsage.COMMUNICATION,
                        previewingUsage = AndroidAudioUsage.ALARM,
                    ),
                    flow.uiState.first(),
                )
                assertEquals(index + 1, usageCollections)
                assertEquals(index + 1, previewCollections)
            }
        }
}
