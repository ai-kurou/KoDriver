package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DebugStateCardKey
import kurou.kodriver.domain.repository.DebugStateCardOrderPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveDebugStateCardOrderUseCaseTest {
    private val repository: DebugStateCardOrderPreferencesRepository = mockk()

    @Test
    fun `Repositoryが返す順序をそのまま返す`() =
        runTest {
            val order = listOf(DebugStateCardKey.SESSION, DebugStateCardKey.SIMULATOR)
            every { repository.observeCardOrder() } returns MutableStateFlow(order)
            val useCase = ObserveDebugStateCardOrderUseCase(repository)

            assertEquals(order, useCase().first())
            verify(exactly = 1) { repository.observeCardOrder() }
            confirmVerified(repository)
        }
}
