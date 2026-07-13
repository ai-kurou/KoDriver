package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
        val serverIpRepository = mockk<ServerIpPreferencesRepository>()
        val simulatorRepository = mockk<SimulatorPreferencesRepository>()
        every { serverIpRepository.serverIp() } returns MutableStateFlow(null)
        every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
        val useCase = createUseCase(
            serverIpRepository = serverIpRepository,
            simulatorRepository = simulatorRepository,
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
        val versionRepository = mockk<ServerVersionRepository>()
        coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
        val useCase = createUseCase(versionRepository = versionRepository)

        val states = mutableListOf<KoDriverServerConnectionState>()
        val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
        delay(50L)

        assertEquals(KoDriverServerConnectionStatus.CHECKING, states[0].connectionStatus)
        assertEquals(KoDriverServerConnectionStatus.CONNECTED, states[1].connectionStatus)
        assertEquals("1.0.0", states[1].serverVersion)
        assertFalse(states[1].isVersionMismatch)
        coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
        job.cancel()
    }

    @Test
    fun `サーバーバージョンがアプリバージョンと異なる場合は不一致を返す`() = runBlocking {
        val versionRepository = mockk<ServerVersionRepository>()
        coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("2.0.0")
        val useCase = createUseCase(versionRepository = versionRepository)

        val states = mutableListOf<KoDriverServerConnectionState>()
        val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
        delay(50L)

        assertTrue(states[1].isVersionMismatch)
        job.cancel()
    }

    @Test
    fun `一定間隔で接続状態を更新する`() = runBlocking {
        val versionRepository = mockk<ServerVersionRepository>()
        coEvery { versionRepository.fetchVersion("192.168.1.1") } returnsMany listOf(
            Result.failure(RuntimeException("down")),
            Result.success("1.0.0"),
        )
        val useCase = createUseCase(versionRepository = versionRepository)

        val states = mutableListOf<KoDriverServerConnectionState>()
        val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
        delay(50L)
        assertEquals(KoDriverServerConnectionStatus.DISCONNECTED, states[1].connectionStatus)

        delay(1_050L)

        assertEquals(KoDriverServerConnectionStatus.CONNECTED, states[2].connectionStatus)
        coVerify(exactly = 2) { versionRepository.fetchVersion("192.168.1.1") }
        job.cancel()
    }

    private fun createUseCase(
        serverIpRepository: ServerIpPreferencesRepository = mockk {
            every { serverIp() } returns MutableStateFlow("192.168.1.1")
        },
        simulatorRepository: SimulatorPreferencesRepository = mockk {
            every { selectedSimulator() } returns MutableStateFlow(null)
        },
        versionRepository: ServerVersionRepository = mockk {
            coEvery { fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
        },
    ) = ObserveKoDriverServerConnectionUseCase(
        fetchServerVersion = FetchServerVersionUseCase(versionRepository),
        observeServerIp = ObserveServerIpUseCase(serverIpRepository),
        observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
    )
}
