package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutStartSoundType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutStartSoundRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_start_sound_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は FORMULA_RADIO`() = testScope.runTest {
        val repository = createReadoutStartSoundRepository(tempDir.absolutePath)

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, repository.observeType().first())
    }

    @Test
    fun `saveType した値を observeType で取得できる`() = testScope.runTest {
        val repository = createReadoutStartSoundRepository(tempDir.absolutePath)

        repository.saveType(ReadoutStartSoundType.FORMULA_RADIO)

        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, repository.observeType().first())
    }

    @Test
    fun `readout_start_sound_preferences_pb に書き込まれる`() = testScope.runTest {
        val repository = createReadoutStartSoundRepository(tempDir.absolutePath)
        repository.saveType(ReadoutStartSoundType.ELECTRONIC_NOISE)
        repository.observeType().first()

        assertTrue(tempDir.resolve("readout_start_sound_preferences.pb").exists())
    }

    @Test
    fun `異なる種別を連続して保存できる`() = testScope.runTest {
        val repository = createReadoutStartSoundRepository(tempDir.absolutePath)

        repository.saveType(ReadoutStartSoundType.FORMULA_RADIO)
        assertEquals(ReadoutStartSoundType.FORMULA_RADIO, repository.observeType().first())

        repository.saveType(ReadoutStartSoundType.ELECTRONIC_NOISE)
        assertEquals(ReadoutStartSoundType.ELECTRONIC_NOISE, repository.observeType().first())
    }
}
