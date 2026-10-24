package kurou.kodriver.feature.main

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LowVolumeWarningUiStateTest {
    @Test
    fun `音量警告がない場合は非表示`() {
        assertFalse(LowVolumeWarningUiState().isVisible)
    }

    @Test
    fun `端末音量警告がある場合は表示`() {
        assertTrue(LowVolumeWarningUiState(deviceVolume = 20).isVisible)
    }

    @Test
    fun `KoDriver音量警告がある場合は表示`() {
        assertTrue(LowVolumeWarningUiState(soundVolume = 20).isVisible)
    }
}
