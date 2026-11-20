package kurou.kodriver.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.outlined.MusicNote
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadoutStartSoundIconsTest {
    @Test
    fun `設定一覧用のOutlined様式は開始音ありのMusicNoteを返す`() {
        assertEquals(Icons.Outlined.MusicNote, ReadoutStartSoundIcons.OutlinedOn)
    }

    @Test
    fun `自動読み上げ用のFilled様式は開始音ありのMusicNoteを返す`() {
        assertEquals(Icons.Filled.MusicNote, ReadoutStartSoundIcons.FilledOn)
    }

    @Test
    fun `自動読み上げ用のFilled様式は開始音なしのMusicOffを返す`() {
        assertEquals(Icons.Filled.MusicOff, ReadoutStartSoundIcons.FilledOff)
    }
}
