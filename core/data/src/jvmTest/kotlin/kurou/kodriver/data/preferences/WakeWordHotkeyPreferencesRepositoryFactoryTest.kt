package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class WakeWordHotkeyPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_wake_word_hotkey_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は未割り当てと既定の修飾キー`() =
        runTest {
            val repository = createWakeWordHotkeyPreferencesRepository(tempDir.absolutePath)

            assertEquals(WakeWordHotkey(), repository.observeWakeWordHotkey().first())
        }

    @Test
    fun `保存した割り当てを読み出せて解除もできる`() =
        runTest {
            val repository = createWakeWordHotkeyPreferencesRepository(tempDir.absolutePath)
            val assigned = WakeWordHotkey(0x41, ManualReadoutHotkeyModifiers.ALT_SHIFT)
            repository.saveWakeWordHotkey(assigned)

            assertEquals(assigned, repository.observeWakeWordHotkey().first())

            repository.saveWakeWordHotkey(assigned.copy(virtualKeyCode = null))

            assertEquals(assigned.copy(virtualKeyCode = null), repository.observeWakeWordHotkey().first())
        }
}
