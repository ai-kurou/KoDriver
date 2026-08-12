package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class Gt7Ps5UdpPortPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `UDPポート設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createGt7Ps5UdpPortPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(port = 33741) }

            assertTrue(tempDir.resolve("gt7_udp_port_preferences.pb").exists())
        }
}
