@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidServerIpPreferencesRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidServerIpPreferencesRepository

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("server_ip_test", ".preferences_pb")
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(testDispatcher + SupervisorJob()),
                produceFile = { tempFile },
            )
        repository = AndroidServerIpPreferencesRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `serverIpは初期状態でnullを返し保存後にIPアドレスを返す`() =
        runTest(testDispatcher) {
            assertNull(repository.serverIp().first())

            repository.saveServerIp("192.168.1.10")

            assertEquals("192.168.1.10", repository.serverIp().first())
        }
}
