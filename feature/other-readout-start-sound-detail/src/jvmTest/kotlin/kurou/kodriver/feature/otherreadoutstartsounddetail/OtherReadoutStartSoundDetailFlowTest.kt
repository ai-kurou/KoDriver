package kurou.kodriver.feature.otherreadoutstartsounddetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherReadoutStartSoundDetailFlowTest {
    private val savedType = MutableStateFlow(ReadoutStartSoundType.FORMULA_RADIO)
    private val pendingType = MutableStateFlow<ReadoutStartSoundType?>(null)

    private fun createFlow() =
        OtherReadoutStartSoundDetailFlow(
            savedType = savedType,
            pendingType = pendingType,
        )

    @Test
    fun `保存値を選択済み種別とし未確定の選択がなければ保存値を表示する`() =
        runTest {
            val flow = createFlow()
            for (saved in ReadoutStartSoundType.entries) {
                for (pending in listOf(null) + ReadoutStartSoundType.entries) {
                    savedType.update { saved }
                    pendingType.update { pending }
                    assertEquals(
                        OtherReadoutStartSoundDetailUiState(
                            selectedType = saved,
                            pendingType = pending ?: saved,
                        ),
                        flow.uiState.first(),
                    )
                }
            }
        }

    @Test
    fun `収集するまで入力を購読せず収集ごとに最新の状態を合成する`() =
        runTest {
            var subscriptions = 0
            val flow =
                OtherReadoutStartSoundDetailFlow(
                    savedType =
                        flow {
                            subscriptions++
                            emit(savedType.value)
                        },
                    pendingType = pendingType,
                )

            assertEquals(0, subscriptions)
            assertEquals(OtherReadoutStartSoundDetailUiState(), flow.uiState.first())
            assertEquals(1, subscriptions)

            pendingType.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
            assertEquals(
                OtherReadoutStartSoundDetailUiState(pendingType = ReadoutStartSoundType.ELECTRONIC_NOISE),
                flow.uiState.first(),
            )

            savedType.update { ReadoutStartSoundType.ELECTRONIC_NOISE }
            pendingType.update { null }
            assertEquals(
                OtherReadoutStartSoundDetailUiState(
                    selectedType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                    pendingType = ReadoutStartSoundType.ELECTRONIC_NOISE,
                ),
                flow.uiState.first(),
            )
            assertEquals(3, subscriptions)
        }
}
