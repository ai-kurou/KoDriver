package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsRainPreferencesRepositoryFactoryTest {
    @Test
    fun `createLmuWindowsRainPreferencesRepository は保存と取得が正常に動作する`() =
        runTest {
            val tempDir = Files.createTempDirectory("kodriver_rain_pref_factory_test").toFile()
            try {
                val repo = createLmuWindowsRainPreferencesRepository(tempDir.absolutePath)

                assertTrue(repo.observeRainEnabledStates().first().isEmpty())

                repo.saveRainEnabledState(ReadoutItemKey.LmuWindows.Rain.Start, false)
                assertEquals(
                    mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.Rain.Start to false),
                    repo.observeRainEnabledStates().first(),
                )
            } finally {
                tempDir.deleteRecursively()
            }
        }
}
