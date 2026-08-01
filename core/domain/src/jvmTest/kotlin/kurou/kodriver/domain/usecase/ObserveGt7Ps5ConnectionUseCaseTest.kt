package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveGt7Ps5ConnectionUseCaseTest {

    @MockK
    private lateinit var repository: Gt7Ps5Repository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `接続確認結果とテレメトリを返す`() =
        runBlocking {
        val telemetry =
            Gt7Ps5TelemetryData(
            lapCount = 3,
            lapsInRace = 10,
            bestLapTimeMs = 0,
            gasLevel = 20f,
            gasCapacity = 50f,
        )
        every { repository.telemetryStream() } returns MutableStateFlow(telemetry)
        coEvery { repository.isConnected() } returns true
        val useCase = createUseCase(repository)

        val state = withTimeout(1_000L) { useCase().first { it.telemetry == telemetry } }

        assertTrue(state.isConnected)
        assertEquals(telemetry, state.telemetry)
        coVerify(exactly = 1) { repository.isConnected() }
        verify(exactly = 1) { repository.telemetryStream() }
        confirmVerified(repository)
    }

    @Test
    fun `接続確認で例外が発生した場合は未接続として監視を継続する`() =
        runBlocking {
        every { repository.telemetryStream() } returns emptyFlow()
        coEvery { repository.isConnected() } throws RuntimeException("connection check failed") andThen true
        val useCase = createUseCase(repository)

        val states = mutableListOf<Gt7Ps5ConnectionState>()
        val job = launch { useCase().collect { states += it } }
        delay(50L)
        assertFalse(states.first().isConnected)
        assertNull(states.first().telemetry)

        delay(1_050L)

        assertTrue(states.last().isConnected)
        coVerify(exactly = 2) { repository.isConnected() }
        job.cancel()
        verify(exactly = 1) { repository.telemetryStream() }
        confirmVerified(repository)
    }

    private fun createUseCase(repository: Gt7Ps5Repository) =
        ObserveGt7Ps5ConnectionUseCase(
        checkGt7Ps5Connection = CheckGt7Ps5ConnectionUseCase(repository),
        observeGt7Ps5 = ObserveGt7Ps5UseCase(repository),
    )
}
