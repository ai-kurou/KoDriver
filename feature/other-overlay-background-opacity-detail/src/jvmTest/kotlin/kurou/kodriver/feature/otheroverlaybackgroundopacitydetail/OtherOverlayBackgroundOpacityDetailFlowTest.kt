package kurou.kodriver.feature.otheroverlaybackgroundopacitydetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OVERLAY_BACKGROUND_OPACITY_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherOverlayBackgroundOpacityDetailFlowTest {
    private val opacityFlow = MutableStateFlow(OVERLAY_BACKGROUND_OPACITY_DEFAULT)

    private fun createFlow() =
        OtherOverlayBackgroundOpacityDetailFlow(
            observedOverlayBackgroundOpacity = opacityFlow,
        )

    @Test
    fun `初期値から画面状態を生成する`() =
        runTest {
            val flow = createFlow()

            assertEquals(OtherOverlayBackgroundOpacityDetailUiState(), flow.uiState.first())
        }

    @Test
    fun `透明度の更新を画面状態に反映する`() =
        runTest {
            val flow = createFlow()
            assertEquals(OtherOverlayBackgroundOpacityDetailUiState(), flow.uiState.first())

            for (opacity in listOf(0, 80, 100)) {
                opacityFlow.update { opacity }
                assertEquals(OtherOverlayBackgroundOpacityDetailUiState(opacity = opacity), flow.uiState.first())
            }
        }
}
