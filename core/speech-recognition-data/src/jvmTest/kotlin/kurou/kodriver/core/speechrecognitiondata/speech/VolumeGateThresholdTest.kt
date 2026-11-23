package kurou.kodriver.core.speechrecognitiondata.speech

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class VolumeGateThresholdTest {
    private val repository = FakeMicrophoneCutQuietSoundPreferencesRepository()

    @Test
    fun `段階値を閾値にする`() {
        assertEquals(0f, 0.toVolumeGateThreshold())
        assertEquals(0.3f, 6.toVolumeGateThreshold(), 0.0001f)
        assertEquals(0.6f, 12.toVolumeGateThreshold(), 0.0001f)
    }

    @Test
    fun `保存済みの設定を閾値として渡し、設定の変更を反映する`() =
        runTest(UnconfinedTestDispatcher()) {
            val result =
                repository.withVolumeGateThreshold { threshold ->
                    val initial = threshold.value
                    repository.saveMicrophoneCutQuietSoundLevel(12)
                    initial to threshold.value
                }

            assertEquals(0.3f, result.first, 0.0001f)
            assertEquals(0.6f, result.second, 0.0001f)
        }

    @Test
    fun `ブロックを抜けたら設定の購読をやめる`() =
        runTest(UnconfinedTestDispatcher()) {
            repository.withVolumeGateThreshold { }

            assertEquals(0, repository.level.subscriptionCount.value)
        }
}
