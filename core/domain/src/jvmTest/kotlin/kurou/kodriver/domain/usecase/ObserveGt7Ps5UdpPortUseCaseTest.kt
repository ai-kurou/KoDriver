package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5UdpPortPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5UdpPortUseCaseTest {

    @MockK
    private lateinit var repo: Gt7Ps5UdpPortPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runBlocking {
        val state = MutableStateFlow(33740)
        every { repo.port() } returns state
        listOf(33740, 33741).forEach { port ->
            coEvery { repo.savePort(port) } answers { state.update { port } }
        }
        val useCase = ObserveGt7Ps5UdpPortUseCase(repo)

        assertEquals(33740, useCase().first())

        repo.savePort(33741)
        assertEquals(33741, useCase().first())

        verify(exactly = 2) { repo.port() }
        coVerify(exactly = 1) { repo.savePort(33741) }
        confirmVerified(repo)
    }
}
