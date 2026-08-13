package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ThemeMode
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_theme_preferences_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はSYSTEM`() =
        runTest {
            val repository = createThemePreferencesRepository(tempDir.absolutePath)

            assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
        }

    @Test
    fun `保存したテーマモードを読み出せる`() =
        runTest {
            val repository = createThemePreferencesRepository(tempDir.absolutePath)

            repository.saveThemeMode(ThemeMode.DARK)

            assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())
        }
}
