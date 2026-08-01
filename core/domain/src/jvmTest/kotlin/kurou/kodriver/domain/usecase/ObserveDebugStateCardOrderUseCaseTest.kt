package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveDebugStateCardOrderUseCaseTest {

    @MockK
    private lateinit var repository: DebugStateCardOrderPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Repositoryが返す順序をそのまま返す`() =
        runBlocking {
        val order = listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR)
        every { repository.observeCardOrder() } returns MutableStateFlow(order)
        val useCase = ObserveDebugStateCardOrderUseCase(repository)

        assertEquals(order, useCase().first())
        verify(exactly = 1) { repository.observeCardOrder() }
        confirmVerified(repository)
    }
}
