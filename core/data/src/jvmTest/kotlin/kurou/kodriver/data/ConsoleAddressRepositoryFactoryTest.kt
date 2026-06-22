package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ConsoleAddressRepository
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertEquals

class ConsoleAddressRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_console_address_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ConsoleAddressRepositoryを返す`() {
        val repository = createConsoleAddressRepository(tempDir.absolutePath)

        assertIs<ConsoleAddressRepository>(repository)
    }

    @Test
    fun `初期状態はnullを返す`() = runTest {
        val repository = createConsoleAddressRepository(tempDir.absolutePath)

        assertNull(repository.consoleAddress().first())
    }

    @Test
    fun `保存したアドレスを取得できる`() = runTest {
        val repository = createConsoleAddressRepository(tempDir.absolutePath)

        repository.saveConsoleAddress("10.0.0.1")

        assertEquals("10.0.0.1", repository.consoleAddress().first())
    }
}
