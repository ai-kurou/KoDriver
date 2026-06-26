package kurou.kodriver.core.gt7ps5data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7UdpPortPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `gt7_udp_port_preferences_pbに書き込まれる`() = testScope.runTest {
        val repository = createGt7UdpPortPreferencesRepository(tempDir.absolutePath)
        repository.savePort(33741)

        assertEquals(33741, repository.port().first())
    }
}
