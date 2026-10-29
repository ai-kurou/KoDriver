package kurou.kodriver.feature.main

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LowVolumeWarningUiStateTest {
    @Test
    fun `端末音量またはKoDriver音量がある場合だけ表示`() {
        assertTrue(LowVolumeWarningUiState(deviceVolume = 20).isVisible)
        assertTrue(LowVolumeWarningUiState(soundVolume = 20).isVisible)
        assertFalse(LowVolumeWarningUiState().isVisible)
    }
}
