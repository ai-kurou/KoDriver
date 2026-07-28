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
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.repository.AceWindowsFuelRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObserveAceWindowsConnectionUseCaseTest {

    @MockK
    private lateinit var repository: AceWindowsFuelRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `接続確認結果と燃料データを返す`() = runBlocking {
        val fuel = AceWindowsFuelData(remainingPercent = 42.0)
        every { repository.fuelStream() } returns MutableStateFlow(fuel)
        coEvery { repository.isConnected() } returns true
        val useCase = createUseCase(repository)

        val state = withTimeout(1_000L) { useCase().first { it.fuel == fuel } }

        assertTrue(state.isConnected)
        assertEquals(fuel, state.fuel)
        coVerify(exactly = 1) { repository.isConnected() }
        verify(exactly = 1) { repository.fuelStream() }
        confirmVerified(repository)
    }

    @Test
    fun `接続確認で例外が発生した場合は未接続として監視を継続する`() = runBlocking {
        every { repository.fuelStream() } returns emptyFlow()
        coEvery { repository.isConnected() } throws RuntimeException("connection check failed") andThen true
        val useCase = createUseCase(repository)

        val states = mutableListOf<AceWindowsConnectionState>()
        val job = launch { useCase().collect { states += it } }
        delay(50L)
        assertFalse(states.first().isConnected)
        assertNull(states.first().fuel)

        delay(1_050L)

        assertTrue(states.last().isConnected)
        coVerify(exactly = 2) { repository.isConnected() }
        job.cancel()
        verify(exactly = 1) { repository.fuelStream() }
        confirmVerified(repository)
    }

    private fun createUseCase(repository: AceWindowsFuelRepository) = ObserveAceWindowsConnectionUseCase(
        checkAceWindowsConnection = CheckAceWindowsConnectionUseCase(repository),
        observeAceWindowsFuel = ObserveAceWindowsFuelUseCase(repository),
    )
}
