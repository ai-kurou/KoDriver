package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class CorePreferencesDataStoreFactoriesTest {
    private val tempDir = Files.createTempDirectory("kodriver_preferences_factories_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private suspend fun assertWritesTo(
        fileName: String,
        write: suspend (directory: String) -> Unit,
    ) {
        val directory = tempDir.resolve(fileName).apply { mkdirs() }
        write(directory.absolutePath)
        assertTrue(File(directory, fileName).exists(), "$fileName が作成されていない")
    }

    @Test
    fun `各ファクトリの設定が期待するファイルに書き込まれる`() =
        runTest {
            assertWritesTo("auto_readout_preferences.pb") { dir ->
                val dataStore = createAutoReadoutPreferencesDataStore(dir)
                dataStore.updateData { prefs ->
                    prefs.copy(simulatorStates = prefs.simulatorStates + ("lmu_windows" to SimulatorAutoReadoutState()))
                }
            }
            assertWritesTo("low_volume_warning_enabled_preferences.pb") { dir ->
                val dataStore = createLowVolumeWarningEnabledPreferencesDataStore(dir)
                dataStore.updateData { it.copy(enabled = false) }
            }
            assertWritesTo("manual_readout_play_button_layout_preferences.pb") { dir ->
                val dataStore = createManualReadoutPlayButtonLayoutPreferencesDataStore(dir)
                dataStore.updateData { it.copy(enabled = true) }
            }
            assertWritesTo("overlay_background_opacity_preferences.pb") { dir ->
                val dataStore = createOverlayBackgroundOpacityPreferencesDataStore(dir)
                dataStore.updateData { it.copy(opacity = 60) }
            }
            assertWritesTo("overlay_text_color_preferences.pb") { dir ->
                val dataStore = createOverlayTextColorPreferencesDataStore(dir)
                dataStore.updateData { it.copy(argb = 0xFF123456L) }
            }
            assertWritesTo("overlay_visible_preferences.pb") { dir ->
                val dataStore = createOverlayVisiblePreferencesDataStore(dir)
                dataStore.updateData { it.copy(visible = false) }
            }
            assertWritesTo("overlay_window_bounds_preferences.pb") { dir ->
                val dataStore = createOverlayWindowBoundsPreferencesDataStore(dir)
                dataStore.updateData { it.copy(x = 10, y = 20) }
            }
            assertWritesTo("simulator_preferences.pb") { dir ->
                val dataStore = createSimulatorPreferencesDataStore(dir)
                dataStore.updateData { it.copy(selectedSimulator = "lmu_windows") }
            }
            assertWritesTo("sound_volume_preferences.pb") { dir ->
                val dataStore = createSoundVolumePreferencesDataStore(dir)
                dataStore.updateData { it.copy(volume = 60) }
            }
            assertWritesTo("voice_pitch_preferences.pb") { dir ->
                val dataStore = createVoicePitchPreferencesDataStore(dir)
                dataStore.updateData { it.copy(voicePitch = 1.5f) }
            }
            assertWritesTo("voice_preferences.pb") { dir ->
                val dataStore = createVoicePreferencesDataStore(dir)
                dataStore.updateData { it.copy(voiceId = "ja-jp-x-jab-local") }
            }
            assertWritesTo("voice_speed_preferences.pb") { dir ->
                val dataStore = createVoiceSpeedPreferencesDataStore(dir)
                dataStore.updateData { it.copy(voiceSpeed = 1.5f) }
            }
        }
}
