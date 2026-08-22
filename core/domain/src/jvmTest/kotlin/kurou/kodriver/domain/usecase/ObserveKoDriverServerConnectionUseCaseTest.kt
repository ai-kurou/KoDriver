package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.CONNECTION_CHECK_INTERVAL_MS_DEFAULT
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.repository.ServerIpPreferencesRepository
import kurou.kodriver.domain.repository.ServerVersionRepository
import kurou.kodriver.domain.repository.SimulatorPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveKoDriverServerConnectionUseCaseTest {
    @MockK
    private lateinit var serverIpRepository: ServerIpPreferencesRepository

    @MockK
    private lateinit var simulatorRepository: SimulatorPreferencesRepository

    @MockK
    private lateinit var versionRepository: ServerVersionRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `IP未設定時は未設定状態を返す`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow(null)
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(Simulator.LmuWindows)
            val useCase = createUseCase()

            val state = useCase(appVersion = "1.0.0").first()

            assertEquals(KoDriverServerConnectionStatus.NOT_CONFIGURED, state.connectionStatus)
            assertTrue(state.requiresKoDriverServer)
            assertEquals(Simulator.LmuWindows, state.selectedSimulator)
            assertNull(state.serverVersion)
            assertFalse(state.isVersionMismatch)
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, simulatorRepository, versionRepository)
        }

    @Test
    fun `接続成功時は接続済み状態とサーバーバージョンを返す`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("1.0.0")
            val useCase = createUseCase()

            val states = mutableListOf<KoDriverServerConnectionState>()
            val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
            delay(50L)

            assertEquals(KoDriverServerConnectionStatus.CHECKING, states[0].connectionStatus)
            assertEquals(KoDriverServerConnectionStatus.CONNECTED, states[1].connectionStatus)
            assertEquals("1.0.0", states[1].serverVersion)
            assertFalse(states[1].isVersionMismatch)
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            job.cancel()
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, simulatorRepository, versionRepository)
        }

    @Test
    fun `サーバーバージョンがアプリバージョンと異なる場合は不一致を返す`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returns Result.success("2.0.0")
            val useCase = createUseCase()

            val states = mutableListOf<KoDriverServerConnectionState>()
            val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
            delay(50L)

            assertTrue(states[1].isVersionMismatch)
            job.cancel()
            coVerify(exactly = 1) { versionRepository.fetchVersion("192.168.1.1") }
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, simulatorRepository, versionRepository)
        }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun `一定間隔で接続状態を更新する`() =
        runTest {
            every { serverIpRepository.serverIp() } returns MutableStateFlow("192.168.1.1")
            every { simulatorRepository.selectedSimulator() } returns MutableStateFlow(null)
            coEvery { versionRepository.fetchVersion("192.168.1.1") } returnsMany
                listOf(
                    Result.failure(RuntimeException("down")),
                    Result.success("1.0.0"),
                )
            val useCase = createUseCase()

            val states = mutableListOf<KoDriverServerConnectionState>()
            val job = launch { useCase(appVersion = "1.0.0").collect { states += it } }
            runCurrent()
            assertEquals(KoDriverServerConnectionStatus.DISCONNECTED, states[1].connectionStatus)

            advanceTimeBy(CONNECTION_CHECK_INTERVAL_MS_DEFAULT)
            runCurrent()

            assertEquals(KoDriverServerConnectionStatus.CONNECTED, states[2].connectionStatus)
            coVerify(exactly = 2) { versionRepository.fetchVersion("192.168.1.1") }
            job.cancel()
            verify(exactly = 1) { serverIpRepository.serverIp() }
            verify(exactly = 1) { simulatorRepository.selectedSimulator() }
            confirmVerified(serverIpRepository, simulatorRepository, versionRepository)
        }

    private fun createUseCase() =
        ObserveKoDriverServerConnectionUseCase(
            fetchServerVersion = FetchServerVersionUseCase(versionRepository),
            observeServerIp = ObserveServerIpUseCase(serverIpRepository),
            observeSelectedSimulator = ObserveSelectedSimulatorUseCase(simulatorRepository),
        )
}
