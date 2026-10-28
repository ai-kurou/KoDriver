package kurou.kodriver.feature.otheroverlaytextcolordetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherOverlayTextColorDetailFlowTest {
    private val observedOverlayTextColor = MutableStateFlow(OVERLAY_TEXT_COLOR_DEFAULT)
    private val pendingOverlayTextColor = MutableStateFlow<Long?>(null)

    private fun createFlow() =
        OtherOverlayTextColorDetailFlow(
            observedOverlayTextColor = observedOverlayTextColor,
            pendingOverlayTextColor = pendingOverlayTextColor,
        )

    @Test
    fun `未保存の選択がなければ観測値とその更新を両方の色に反映する`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherOverlayTextColorDetailUiState(), flow.uiState.first())

            observedOverlayTextColor.update { 0xFF123456L }
            assertEquals(
                OtherOverlayTextColorDetailUiState(
                    selectedOverlayTextColor = 0xFF123456L,
                    pendingOverlayTextColor = 0xFF123456L,
                ),
                flow.uiState.first(),
            )
        }

    @Test
    fun `未保存の選択を優先し観測値が変わっても保持して解除時に観測値へ戻す`() =
        runTest {
            val flow = createFlow()
            pendingOverlayTextColor.update { 0L }
            assertEquals(
                OtherOverlayTextColorDetailUiState(pendingOverlayTextColor = 0L),
                flow.uiState.first(),
            )

            observedOverlayTextColor.update { 0xFFFFFFFFL }
            assertEquals(
                OtherOverlayTextColorDetailUiState(
                    selectedOverlayTextColor = 0xFFFFFFFFL,
                    pendingOverlayTextColor = 0L,
                ),
                flow.uiState.first(),
            )

            pendingOverlayTextColor.update { 0xFFFF0000L }
            assertEquals(
                OtherOverlayTextColorDetailUiState(
                    selectedOverlayTextColor = 0xFFFFFFFFL,
                    pendingOverlayTextColor = 0xFFFF0000L,
                ),
                flow.uiState.first(),
            )

            pendingOverlayTextColor.update { null }
            assertEquals(
                OtherOverlayTextColorDetailUiState(
                    selectedOverlayTextColor = 0xFFFFFFFFL,
                    pendingOverlayTextColor = 0xFFFFFFFFL,
                ),
                flow.uiState.first(),
            )
        }

    @Test
    fun `購読するまで入力を収集せず購読ごとに収集する`() =
        runTest {
            var collections = 0
            val flow =
                OtherOverlayTextColorDetailFlow(
                    observedOverlayTextColor =
                        flow {
                            collections++
                            emit(OVERLAY_TEXT_COLOR_DEFAULT)
                        },
                    pendingOverlayTextColor = pendingOverlayTextColor,
                )
            assertEquals(0, collections)
            repeat(2) { index ->
                assertEquals(OtherOverlayTextColorDetailUiState(), flow.uiState.first())
                assertEquals(index + 1, collections)
            }
        }
}
