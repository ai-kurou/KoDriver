package kurou.kodriver.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.createConsoleAddressDataStore
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ConsoleAddressRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_console_address_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private fun createRepository() = ConsoleAddressRepositoryImpl(
        dataStore = createConsoleAddressDataStore(tempDir.absolutePath),
    )

    @Test
    fun `アドレスを保存して取得できる`() = testScope.runTest {
        val repository = createRepository()
        repository.saveConsoleAddress("192.168.1.100")

        assertEquals("192.168.1.100", repository.consoleAddress().first())
    }

    @Test
    fun `未保存の場合はnullを返す`() = testScope.runTest {
        val repository = createRepository()

        assertNull(repository.consoleAddress().first())
    }

    @Test
    fun `アドレスを上書き保存できる`() = testScope.runTest {
        val repository = createRepository()
        repository.saveConsoleAddress("192.168.1.1")
        repository.saveConsoleAddress("10.0.0.50")

        assertEquals("10.0.0.50", repository.consoleAddress().first())
    }

    @Test
    fun `console_address_pbに書き込まれる`() = testScope.runTest {
        val repository = createRepository()
        repository.saveConsoleAddress("192.168.0.1")

        val file = tempDir.resolve("console_address.pb")
        assert(file.exists()) { "console_address.pb が作成されていない" }
    }
}
