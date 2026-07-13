package kurou.kodriver.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kurou.kodriver.domain.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveGt7Ps5ConnectionUseCaseTest {

    @Test
    fun `接続確認結果とテレメトリを返す`() = runBlocking {
        val telemetry = Gt7Ps5TelemetryData(
            lapCount = 3,
            lapsInRace = 10,
            bestLapTimeMs = 0,
            gasLevel = 20f,
            gasCapacity = 50f,
        )
        val repository = FakeGt7Ps5Repository(
            isConnectedResults = listOf(true),
            telemetryFlow = MutableStateFlow(telemetry),
        )
        val useCase = createUseCase(repository)

        val state = withTimeout(1_000L) { useCase().first { it.telemetry == telemetry } }

        assertTrue(state.isConnected)
        assertEquals(telemetry, state.telemetry)
        assertEquals(1, repository.connectionCheckCount)
    }

    @Test
    fun `接続確認で例外が発生した場合は未接続として監視を継続する`() = runBlocking {
        val repository = FakeGt7Ps5Repository(
            isConnectedResults = listOf(null, true),
            telemetryFlow = emptyFlow(),
        )
        val useCase = createUseCase(repository)

        val states = mutableListOf<Gt7Ps5ConnectionState>()
        val job = launch { useCase().collect { states += it } }
        delay(50L)
        assertFalse(states.first().isConnected)
        assertNull(states.first().telemetry)

        delay(1_050L)

        assertTrue(states.last().isConnected)
        assertEquals(2, repository.connectionCheckCount)
        job.cancel()
    }

    private fun createUseCase(repository: Gt7Ps5Repository) = ObserveGt7Ps5ConnectionUseCase(
        checkGt7Ps5Connection = CheckGt7Ps5ConnectionUseCase(repository),
        observeGt7Ps5 = ObserveGt7Ps5UseCase(repository),
    )

    private class FakeGt7Ps5Repository(
        private val isConnectedResults: List<Boolean?>,
        private val telemetryFlow: Flow<Gt7Ps5TelemetryData>,
    ) : Gt7Ps5Repository {
        var connectionCheckCount = 0
            private set

        override fun telemetryStream() = telemetryFlow

        override suspend fun isConnected(): Boolean {
            val result = isConnectedResults[connectionCheckCount.coerceAtMost(isConnectedResults.lastIndex)]
            connectionCheckCount++
            return result ?: error("connection check failed")
        }
    }
}
