package kurou.kodriver.data.datasource

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.ConsoleAddressPreferences
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleAddressDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_console_address_ds_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `console_address_pbにデータを保存できる`() = runTest {
        val dataStore = createConsoleAddressDataStore(tempDir.absolutePath)
        val expected = ConsoleAddressPreferences(address = "192.168.1.1")

        dataStore.updateData { expected }

        assertEquals(expected, dataStore.data.first())
    }

    @Test
    fun `ファイルパスはdirectory配下のconsole_address_pbである`() = runTest {
        val dataStore = createConsoleAddressDataStore(tempDir.absolutePath)

        dataStore.updateData { ConsoleAddressPreferences(address = "192.168.1.1") }

        val file = tempDir.resolve("console_address.pb")
        assert(file.exists()) { "console_address.pb が作成されていない" }
    }
}
