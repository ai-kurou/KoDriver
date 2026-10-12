package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.OverlayTextColorPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveOverlayTextColorUseCaseTest {
    private val repo: OverlayTextColorPreferencesRepository = mockk()

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(0xFF123456L)
            every { repo.observeOverlayTextColor() } returns state
            listOf(0xFFFFC107L).forEach { argb ->
                coEvery { repo.saveOverlayTextColor(argb) } answers { state.update { argb } }
            }
            val useCase = ObserveOverlayTextColorUseCase(repo)

            assertEquals(0xFF123456L, useCase().first())

            repo.saveOverlayTextColor(0xFFFFC107L)
            assertEquals(0xFFFFC107L, useCase().first())

            verify(exactly = 2) { repo.observeOverlayTextColor() }
            coVerify(exactly = 1) { repo.saveOverlayTextColor(0xFFFFC107L) }
            confirmVerified(repo)
        }
}
