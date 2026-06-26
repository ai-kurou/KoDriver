package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7UdpPortPreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `UDPポート設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createGt7UdpPortPreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(port = 33741) }

        assertTrue(tempDir.resolve("gt7_udp_port_preferences.pb").exists())
    }
}
