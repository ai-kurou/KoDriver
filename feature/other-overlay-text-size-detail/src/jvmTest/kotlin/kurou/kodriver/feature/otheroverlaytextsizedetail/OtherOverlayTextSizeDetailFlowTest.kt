package kurou.kodriver.feature.otheroverlaytextsizedetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherOverlayTextSizeDetailFlowTest {
    private val observedOverlayTextSize = MutableStateFlow(OverlayTextSize.MEDIUM)
    private val pendingOverlayTextSize = MutableStateFlow<OverlayTextSize?>(null)

    private fun createFlow() =
        OtherOverlayTextSizeDetailFlow(
            observedOverlayTextSize = observedOverlayTextSize,
            pendingOverlayTextSize = pendingOverlayTextSize,
        )

    @Test
    fun `適用中の全サイズと未保存の選択値から画面状態を合成する`() =
        runTest {
            val flow = createFlow()
            for (observed in OverlayTextSize.entries) {
                for (pending in listOf(null) + OverlayTextSize.entries) {
                    observedOverlayTextSize.update { observed }
                    pendingOverlayTextSize.update { pending }
                    assertEquals(
                        OtherOverlayTextSizeDetailUiState(
                            selectedOverlayTextSize = observed,
                            pendingOverlayTextSize = pending ?: observed,
                        ),
                        flow.uiState.first(),
                    )
                }
            }
        }

    @Test
    fun `適用中のサイズの更新と未保存の選択値の変更と解除を反映する`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherOverlayTextSizeDetailUiState(), flow.uiState.first())

            observedOverlayTextSize.update { OverlayTextSize.LARGE }
            assertEquals(
                OtherOverlayTextSizeDetailUiState(OverlayTextSize.LARGE, OverlayTextSize.LARGE),
                flow.uiState.first(),
            )

            pendingOverlayTextSize.update { OverlayTextSize.SMALL }
            assertEquals(
                OtherOverlayTextSizeDetailUiState(OverlayTextSize.LARGE, OverlayTextSize.SMALL),
                flow.uiState.first(),
            )

            observedOverlayTextSize.update { OverlayTextSize.MAXIMUM }
            assertEquals(
                OtherOverlayTextSizeDetailUiState(OverlayTextSize.MAXIMUM, OverlayTextSize.SMALL),
                flow.uiState.first(),
            )

            pendingOverlayTextSize.update { OverlayTextSize.EXTRA_SMALL }
            assertEquals(
                OtherOverlayTextSizeDetailUiState(OverlayTextSize.MAXIMUM, OverlayTextSize.EXTRA_SMALL),
                flow.uiState.first(),
            )

            pendingOverlayTextSize.update { null }
            assertEquals(
                OtherOverlayTextSizeDetailUiState(OverlayTextSize.MAXIMUM, OverlayTextSize.MAXIMUM),
                flow.uiState.first(),
            )
        }
}
