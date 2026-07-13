package kurou.kodriver.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveKoDriverServerConnectionUseCaseTest {

    @Test
    fun `IP未設定時は未設定状態を返す`() = runBlocking {
        val useCase = createUseCase(
            serverIpRepository = FakeServerIpPreferencesRepository(null),
            simulatorRepository = FakeSimulatorPreferencesRepository(Simulator.LmuWindows),
        )

        val state = useCase(appVersion = "1.0.0").first()

        assertEquals(KoDriverServerConnectionStatus.NOT_CONFIGURED, state.connectionStatus)
        assertTrue(state.requiresKoDriverServer)
        assertEquals(Simulator.LmuWindows, state.selectedSimulator)
        assertNull(state.serverVersion)
        assertFalse(state.isVersionMismatch)
    }

    @Test
    fun `接続成功時は接続済み状態とサーバーバージョンを返す`() = runBlocking {
        val versionRepository = FakeServerVersionRepository(listOf(Result.success("1.0.0")))
        val useCase = createUseCase(versionRepository = versionRepository)

        val states = mutableListOf<KoDriverServerConnectionState>()
        val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
        delay(50L)

        assertEquals(KoDriverServerConnectionStatus.CHECKING, states[0].connectionStatus)
        assertEquals(KoDriverServerConnectionStatus.CONNECTED, states[1].connectionStatus)
        assertEquals("1.0.0", states[1].serverVersion)
        assertFalse(states[1].isVersionMismatch)
        assertEquals(listOf("192.168.1.1"), versionRepository.requestedIps)
        job.cancel()
    }

    @Test
    fun `サーバーバージョンがアプリバージョンと異なる場合は不一致を返す`() = runBlocking {
        val useCase = createUseCase(versionRepository = FakeServerVersionRepository(listOf(Result.success("2.0.0"))))

        val states = mutableListOf<KoDriverServerConnectionState>()
        val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
        delay(50L)

        assertTrue(states[1].isVersionMismatch)
        job.cancel()
    }

    @Test
    fun `一定間隔で接続状態を更新する`() = runBlocking {
        val versionRepository = FakeServerVersionRepository(
            listOf(
                Result.failure(RuntimeException("down")),
                Result.success("1.0.0"),
            ),
        )
        val useCase = createUseCase(versionRepository = versionRepository)

        val states = mutableListOf<KoDriverServerConnectionState>()
        val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
        delay(50L)
        assertEquals(KoDriverServerConnectionStatus.DISCONNECTED, states[1].connectionStatus)

        delay(1_050L)

        assertEquals(KoDriverServerConnectionStatus.CONNECTED, states[2].connectionStatus)
        assertEquals(listOf("192.168.1.1", "192.168.1.1"), versionRepository.requestedIps)
        job.cancel()
    }

    private fun createUseCase(
        serverIpRepository: ServerIpPreferencesRepository = FakeServerIpPreferencesRepository("192.168.1.1"),
        simulatorRepository: SimulatorPreferencesRepository = FakeSimulatorPreferencesRepository(null),
        versionRepository: ServerVersionRepository = FakeServerVersionRepository(listOf(Result.success("1.0.0"))),
    ) = ObserveKoDriverServerConnectionUseCase(
        fetchServerVersion = FetchServerVersionUseCase(versionRepository),
        observeServerIp = ObserveServerIpUseCase(serverIpRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )

    private class FakeServerIpPreferencesRepository(ip: String?) : ServerIpPreferencesRepository {
        private val flow = MutableStateFlow(ip)

        override fun serverIp() = flow

        override suspend fun saveServerIp(ip: String) {
            flow.value = ip
        }
    }

    private class FakeSimulatorPreferencesRepository(simulator: Simulator?) : SimulatorPreferencesRepository {
        private val flow = MutableStateFlow(simulator)

        override fun selectedSimulator() = flow

        override suspend fun saveSelectedSimulator(simulator: Simulator) {
            flow.value = simulator
        }
    }

    private class FakeServerVersionRepository(
        private val results: List<Result<String>>,
    ) : ServerVersionRepository {
        val requestedIps = mutableListOf<String>()
        private var index = 0

        override suspend fun fetchVersion(ip: String): Result<String> {
            requestedIps += ip
            return results[index.coerceAtMost(results.lastIndex)].also {
                index++
            }
        }
    }
}
