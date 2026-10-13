package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadoutHotkeyPreferencesRepositoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_readout_hotkey_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `Factoryの初期値はF1で保存と上書きができる`() =
        runTest {
            val repository = createReadoutHotkeyPreferencesRepository(tempDir.absolutePath)
            assertEquals(
                LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT,
                repository.observeVirtualKeyCode().first(),
            )
            repository.saveVirtualKeyCode(0x87)
            assertEquals(0x87, repository.observeVirtualKeyCode().first())
            repository.saveVirtualKeyCode(0x41)
            assertEquals(0x41, repository.observeVirtualKeyCode().first())
            assertTrue(tempDir.resolve("lmu_windows_readout_hotkey_preferences.pb").exists())
            val dataStore = createReadoutHotkeyPreferencesDataStore(tempDir.resolve("other").absolutePath)
            dataStore.updateData { it.copy(virtualKeyCode = 0x30) }
            assertEquals(0x30, ReadoutHotkeyPreferencesRepositoryImpl(dataStore).observeVirtualKeyCode().first())
        }
}
