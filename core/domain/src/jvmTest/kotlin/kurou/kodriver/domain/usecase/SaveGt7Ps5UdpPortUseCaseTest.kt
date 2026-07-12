package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private fun createGt7Ps5UdpPortPreferencesRepository(initial: Int = 33740): Gt7Ps5UdpPortPreferencesRepository {
    val repository = mockk<Gt7Ps5UdpPortPreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.port() } returns state
    coEvery { repository.savePort(any()) } answers { state.update { firstArg() } }
    return repository
}

class SaveGt7Ps5UdpPortUseCaseTest {

    private val repo = createGt7Ps5UdpPortPreferencesRepository()
    private val useCase = SaveGt7Ps5UdpPortUseCase(repo)

    @Test
    fun `33740を保存できる`() = runBlocking {
        useCase(33740)
        assertEquals(33740, repo.port().first())
    }

    @Test
    fun `33741を保存できる`() = runBlocking {
        useCase(33741)
        assertEquals(33741, repo.port().first())
    }

    @Test
    fun `33740でも33741でもない値はIllegalArgumentExceptionをスローする`() = runBlocking<Unit> {
        assertFailsWith<IllegalArgumentException> { runBlocking { useCase(33739) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { useCase(33742) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { useCase(0) } }
    }
}
