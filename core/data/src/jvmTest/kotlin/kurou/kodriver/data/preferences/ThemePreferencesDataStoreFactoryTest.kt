package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_theme_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `theme_preferences_pbを作成してデフォルト値を読み込める`() =
        runTest {
            val dataStore = createThemePreferencesDataStore(tempDir.absolutePath)

            assertEquals("system", dataStore.data.first().mode)
        }
}
