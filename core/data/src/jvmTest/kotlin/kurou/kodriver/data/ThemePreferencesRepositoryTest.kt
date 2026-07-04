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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThemePreferencesRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_theme_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はSYSTEM`() = testScope.runTest {
        val repository = createThemePreferencesRepository(tempDir.absolutePath)

        assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
    }

    @Test
    fun `saveThemeModeした値をobserveThemeModeで取得できる`() = testScope.runTest {
        val repository = createThemePreferencesRepository(tempDir.absolutePath)

        repository.saveThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())
    }

    @Test
    fun `theme_preferences_pbに書き込まれる`() = testScope.runTest {
        val repository = createThemePreferencesRepository(tempDir.absolutePath)

        repository.saveThemeMode(ThemeMode.LIGHT)
        repository.observeThemeMode().first()

        assertTrue(tempDir.resolve("theme_preferences.pb").exists())
    }

    @Test
    fun `異なるテーマモードを連続して保存できる`() = testScope.runTest {
        val repository = createThemePreferencesRepository(tempDir.absolutePath)

        repository.saveThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repository.observeThemeMode().first())

        repository.saveThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())

        repository.saveThemeMode(ThemeMode.SYSTEM)
        assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
    }
}
