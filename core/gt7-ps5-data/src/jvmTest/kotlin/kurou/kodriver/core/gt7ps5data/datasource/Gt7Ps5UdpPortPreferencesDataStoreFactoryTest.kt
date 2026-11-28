package kurou.kodriver.core.gt7ps5data.datasource

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_UDP_PORT_ALTERNATE
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5UdpPortPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_data_store_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `途中で切れた破損ファイルは既定値で読め、保存後の値を読み直せる`() =
        runTest {
            // 長さ5のlength-delimitedフィールドに対して1バイトしかない
            tempDir.resolve(FILE_NAME).writeBytes(byteArrayOf(0x0A, 0x05, 0x61))

            val dataStore = createGt7Ps5UdpPortPreferencesDataStore(tempDir.absolutePath)
            assertEquals(Gt7Ps5UdpPortPreferences(), dataStore.data.first())

            dataStore.updateData { Gt7Ps5UdpPortPreferences(port = GT7_PS5_UDP_PORT_ALTERNATE) }
            assertEquals(
                Gt7Ps5UdpPortPreferences(port = GT7_PS5_UDP_PORT_ALTERNATE),
                dataStore.data.first(),
            )
        }

    @Test
    fun `0埋めの破損ファイルは既定値で読め、保存後の値を読み直せる`() =
        runTest {
            tempDir.resolve(FILE_NAME).writeBytes(ByteArray(8))

            val dataStore = createGt7Ps5UdpPortPreferencesDataStore(tempDir.absolutePath)
            assertEquals(Gt7Ps5UdpPortPreferences(), dataStore.data.first())

            dataStore.updateData { Gt7Ps5UdpPortPreferences(port = GT7_PS5_UDP_PORT_ALTERNATE) }
            assertEquals(
                Gt7Ps5UdpPortPreferences(port = GT7_PS5_UDP_PORT_ALTERNATE),
                dataStore.data.first(),
            )
        }

    private companion object {
        const val FILE_NAME = "gt7_udp_port_preferences.pb"
    }
}
