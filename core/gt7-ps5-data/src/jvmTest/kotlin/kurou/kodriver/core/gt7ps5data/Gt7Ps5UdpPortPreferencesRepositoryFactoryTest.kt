package kurou.kodriver.core.gt7ps5data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5UdpPortPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `gt7_udp_port_preferences_pbに書き込まれる`() =
        runTest {
            val repository = createGt7Ps5UdpPortPreferencesRepository(tempDir.absolutePath)
            repository.savePort(33741)

            assertEquals(33741, repository.port().first())
        }
}
