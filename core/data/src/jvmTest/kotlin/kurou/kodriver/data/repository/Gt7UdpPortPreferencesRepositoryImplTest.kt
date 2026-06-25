package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.Gt7UdpPortPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7UdpPortPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_gt7_udp_port_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = Gt7UdpPortPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = Gt7UdpPortPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は33740・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertEquals(33740, repository.port().first())

        repository.savePort(33741)
        assertEquals(33741, repository.port().first())

        repository.savePort(33740)
        assertEquals(33740, repository.port().first())
    }
}
