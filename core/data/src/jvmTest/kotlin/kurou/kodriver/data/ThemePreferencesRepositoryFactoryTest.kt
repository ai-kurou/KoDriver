package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ThemeMode
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ThemePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_theme_preferences_repository_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はSYSTEM`() =
        testScope.runTest {
            val repository = createThemePreferencesRepository(tempDir.absolutePath)

            assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
        }

    @Test
    fun `保存したテーマモードを読み出せる`() =
        testScope.runTest {
            val repository = createThemePreferencesRepository(tempDir.absolutePath)

            repository.saveThemeMode(ThemeMode.DARK)

            assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())
        }
}
