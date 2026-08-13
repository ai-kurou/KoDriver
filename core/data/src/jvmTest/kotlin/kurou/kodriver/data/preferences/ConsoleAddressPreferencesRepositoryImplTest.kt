package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConsoleAddressPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_console_address_repo_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun createRepository() =
        ConsoleAddressPreferencesRepositoryImpl(
            dataStore = createConsoleAddressDataStore(tempDir.absolutePath),
        )

    @Test
    fun `アドレスを保存して取得できる`() =
        runTest {
            val repository = createRepository()
            repository.saveConsoleAddress("192.168.1.100")

            assertEquals("192.168.1.100", repository.consoleAddress().first())
        }

    @Test
    fun `未保存の場合はnullを返す`() =
        runTest {
            val repository = createRepository()

            assertNull(repository.consoleAddress().first())
        }

    @Test
    fun `アドレスを上書き保存できる`() =
        runTest {
            val repository = createRepository()
            repository.saveConsoleAddress("192.168.1.1")
            repository.saveConsoleAddress("10.0.0.50")

            assertEquals("10.0.0.50", repository.consoleAddress().first())
        }

    @Test
    fun `console_address_pbに書き込まれる`() =
        runTest {
            val repository = createRepository()
            repository.saveConsoleAddress("192.168.0.1")

            val file = tempDir.resolve("console_address.pb")
            assert(file.exists()) { "console_address.pb が作成されていない" }
        }
}
