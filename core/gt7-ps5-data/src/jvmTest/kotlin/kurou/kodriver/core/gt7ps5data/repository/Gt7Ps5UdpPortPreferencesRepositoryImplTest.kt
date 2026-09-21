package kurou.kodriver.core.gt7ps5data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.gt7ps5data.datasource.Gt7Ps5UdpPortPreferencesSerializer
import kurou.kodriver.core.gt7ps5data.model.Gt7Ps5UdpPortPreferences
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_ALTERNATE
import kurou.kodriver.domain.model.GT7_PS5_UDP_PORT_DEFAULT
import java.io.IOException
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `読み取り失敗時はデフォルトポートを1回emitして正常終了する`() =
        testScope.runTest {
            val failingRepository =
                Gt7Ps5UdpPortPreferencesRepositoryImpl(
                    FakeGt7Ps5UdpPortPreferencesDataStore(flow { throw IOException("read failed") }),
                )

            assertEquals(listOf(GT7_PS5_UDP_PORT_DEFAULT), failingRepository.port().toList())
        }

    @Test
    fun `IOException以外の例外はそのまま伝播する`() =
        testScope.runTest {
            val failingRepository =
                Gt7Ps5UdpPortPreferencesRepositoryImpl(
                    FakeGt7Ps5UdpPortPreferencesDataStore(flow { throw IllegalStateException("boom") }),
                )

            assertFailsWith<IllegalStateException> { failingRepository.port().toList() }
        }

    private class FakeGt7Ps5UdpPortPreferencesDataStore(
        override val data: Flow<Gt7Ps5UdpPortPreferences>,
    ) : DataStore<Gt7Ps5UdpPortPreferences> {
        override suspend fun updateData(
            transform: suspend (t: Gt7Ps5UdpPortPreferences) -> Gt7Ps5UdpPortPreferences,
        ): Gt7Ps5UdpPortPreferences = throw UnsupportedOperationException("書き込みはテスト対象外")
    }
}
