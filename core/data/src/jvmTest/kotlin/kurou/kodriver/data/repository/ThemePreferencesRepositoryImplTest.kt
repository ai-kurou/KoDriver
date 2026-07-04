package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ThemePreferencesSerializer
import kurou.kodriver.domain.model.ThemeMode
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ThemePreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_theme_preferences_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = ThemePreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = ThemePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `themeModeの初期値はSYSTEM`() = testScope.runTest {
        assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
    }

    @Test
    fun `saveThemeModeで保存した値をobserveThemeModeで取得できる`() = testScope.runTest {
        repository.saveThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repository.observeThemeMode().first())
    }

    @Test
    fun `saveThemeModeを複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveThemeMode(ThemeMode.LIGHT)
        repository.saveThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.observeThemeMode().first())
    }

    @Test
    fun `themeModeが未知のIDのときSYSTEMを返す`() = testScope.runTest {
        dataStore.updateData { it.copy(mode = "unknown") }

        assertEquals(ThemeMode.SYSTEM, repository.observeThemeMode().first())
    }
}
