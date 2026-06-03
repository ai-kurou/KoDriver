package kurou.kodriver.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = PreferenceDataStoreFactory.createWithPath(
        scope = testScope,
        produceFile = { "${tempDir.absolutePath}/test.preferences_pb".toPath() },
    )
    private val repository = ReadoutPreferencesRepositoryImpl(dataStore)

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeReadoutEnabledStates("lmu").first().isEmpty())

        repository.saveReadoutEnabledState("lmu", "車両接近", true)
        assertEquals(mapOf("車両接近" to true), repository.observeReadoutEnabledStates("lmu").first())

        repository.saveReadoutEnabledState("lmu", "車両接近", false)
        assertEquals(mapOf("車両接近" to false), repository.observeReadoutEnabledStates("lmu").first())
    }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu", "車両接近", true)
        repository.saveReadoutEnabledState("lmu", "残りラップ数", false)

        val states = repository.observeReadoutEnabledStates("lmu").first()
        assertEquals(mapOf("車両接近" to true, "残りラップ数" to false), states)
    }

    @Test
    fun `シミュレーターごとに独立した状態を保存できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu", "車両接近", true)
        repository.saveReadoutEnabledState("rFactor 2", "車両接近", false)

        assertEquals(mapOf("車両接近" to true), repository.observeReadoutEnabledStates("lmu").first())
        assertEquals(mapOf("車両接近" to false), repository.observeReadoutEnabledStates("rFactor 2").first())
    }

    @Test
    fun `ラベルに等号が含まれていても正しく保存・取得できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu", "a=b", true)

        assertEquals(mapOf("a=b" to true), repository.observeReadoutEnabledStates("lmu").first())
    }
}
