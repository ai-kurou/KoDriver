package kurou.kodriver.feature.othermicrophonecutquietsounddetail

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT
import kotlin.test.Test
import kotlin.test.assertEquals

class OtherMicrophoneCutQuietSoundDetailFlowTest {
    private val levelFlow = MutableStateFlow(MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT)

    private fun createFlow() = OtherMicrophoneCutQuietSoundDetailFlow(observedLevel = levelFlow)

    @Test
    fun `初期値から画面状態を生成する`() =
        runTest {
            assertEquals(OtherMicrophoneCutQuietSoundDetailUiState(), createFlow().uiState.first())
        }

    @Test
    fun `段階値の更新を画面状態に反映する`() =
        runTest {
            val flow = createFlow()

            for (level in listOf(0, 3, 12)) {
                levelFlow.update { level }
                assertEquals(OtherMicrophoneCutQuietSoundDetailUiState(level = level), flow.uiState.first())
            }
        }
}
