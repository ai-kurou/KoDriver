package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.Gt7Ps5RemainingFuelLapsEnabledRepository
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserveGt7Ps5RemainingFuelLapsEnabledUseCaseTest {

    @Test
    fun `保存済みの有効状態を返す`() = runBlocking {
        val repository = mockk<Gt7Ps5RemainingFuelLapsEnabledRepository>()
        every { repository.observeEnabled() } returns MutableStateFlow(false)
        val useCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        assertFalse(useCase().first())
    }

    @Test
    fun `未設定の場合はtrueを返す`() = runBlocking {
        val repository = mockk<Gt7Ps5RemainingFuelLapsEnabledRepository>()
        every { repository.observeEnabled() } returns MutableStateFlow(null)
        val useCase = ObserveGt7Ps5RemainingFuelLapsEnabledUseCase(repository)

        assertTrue(useCase().first())
    }
}
