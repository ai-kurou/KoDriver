package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ConsoleAddressPreferencesRepository
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class ConsoleAddressPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_console_address_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ConsoleAddressPreferencesRepositoryを返す`() {
        val repository = createConsoleAddressPreferencesRepository(tempDir.absolutePath)

        assertIs<ConsoleAddressPreferencesRepository>(repository)
    }

    @Test
    fun `初期状態はnullを返す`() = runTest {
        val repository = createConsoleAddressPreferencesRepository(tempDir.absolutePath)

        assertNull(repository.consoleAddress().first())
    }

    @Test
    fun `保存したアドレスを取得できる`() = runTest {
        val repository = createConsoleAddressPreferencesRepository(tempDir.absolutePath)

        repository.saveConsoleAddress("10.0.0.1")

        assertEquals("10.0.0.1", repository.consoleAddress().first())
    }
}
