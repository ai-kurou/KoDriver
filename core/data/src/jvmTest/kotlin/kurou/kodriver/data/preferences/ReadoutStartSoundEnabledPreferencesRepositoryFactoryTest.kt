package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadoutStartSoundEnabledPreferencesRepositoryFactoryTest {
    @Test
    fun `createReadoutStartSoundEnabledPreferencesRepository は保存と取得が正常に動作する`() =
        runTest {
            val tempDir = Files.createTempDirectory("kodriver_start_sound_enabled_pref_factory_test").toFile()
            try {
                val repo = createReadoutStartSoundEnabledPreferencesRepository(tempDir.absolutePath)

                assertTrue(repo.observeStartSoundEnabledStates().first().isEmpty())

                repo.saveStartSoundEnabledState(ReadoutItemKey.LmuWindows.Flag.Root, false)
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Flag.Root to false),
                    repo.observeStartSoundEnabledStates().first(),
                )
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
