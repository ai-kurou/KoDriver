package kurou.kodriver.data

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.exportimport.Exportable
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DataModuleExportablesTest {
    private val tempDir = Files.createTempDirectory("kodriver_data_module_exportables_test").toFile()

    @AfterTest
    fun tearDown() {
        stopKoin()
        tempDir.deleteRecursively()
    }

    @Test
    fun `デスクトップのDataモジュールが対象の設定をExportableとして公開する`() =
        runTest {
            val koin = startKoin { modules(desktopDataModule(tempDir.absolutePath)) }.koin

            val exportables = koin.getAll<Exportable>()

            assertEquals(EXPECTED_KEYS, exportables.map { it.key }.toSortedSet())
            assertEquals(EXPECTED_KEYS.size, exportables.size)
            exportables.forEach { it.export() }
        }

    private companion object {
        // 端末固有の値（ウィンドウ位置・サイズ、マイクのデバイス、接続先、TTS の音声 ID）と
        // 送信時刻の記録は、エクスポート対象にしない。
        val EXPECTED_KEYS =
            sortedSetOf(
                "android_audio_usage",
                "debug_state_card_order.ace_windows",
                "debug_state_card_order.gt7_ps5",
                "debug_state_card_order.lmu_windows",
                "low_volume_warning_enabled",
                "manual_readout_play_button_layout",
                "manual_readout_set.ace_windows",
                "manual_readout_set.gt7_ps5",
                "manual_readout_set.lmu_windows",
                "microphone_cut_quiet_sound",
                "overlay_background_opacity",
                "overlay_text_color",
                "overlay_text_size",
                "overlay_visible",
                "queue",
                "readout_hotkey",
                "readout_start_sound",
                "readout_start_sound_enabled",
                "sound_volume",
                "theme",
                "voice_pitch",
                "voice_speed",
            )
    }
}
