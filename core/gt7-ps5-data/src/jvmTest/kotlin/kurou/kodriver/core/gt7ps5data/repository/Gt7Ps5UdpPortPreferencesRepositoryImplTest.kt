package kurou.kodriver.core.gt7ps5data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpPortPreferencesSerializer
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_ALTERNATE
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5UdpPortPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = Gt7Ps5UdpPortPreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = Gt7Ps5UdpPortPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値はデフォルトポート・保存した値を返す・上書きで更新される`() =
        testScope.runTest {
            assertEquals(GT7_PS5_UDP_PORT_DEFAULT, repository.port().first())

            repository.savePort(GT7_PS5_UDP_PORT_ALTERNATE)
            assertEquals(GT7_PS5_UDP_PORT_ALTERNATE, repository.port().first())

            repository.savePort(GT7_PS5_UDP_PORT_DEFAULT)
            assertEquals(GT7_PS5_UDP_PORT_DEFAULT, repository.port().first())
        }
}
