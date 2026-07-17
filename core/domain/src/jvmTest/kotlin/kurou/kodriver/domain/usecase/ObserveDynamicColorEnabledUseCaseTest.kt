package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.repository.DynamicColorEnabledRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveDynamicColorEnabledUseCaseTest {

    @Test
    fun `Repositoryの値を返す`() = runBlocking {
        val repository = mockk<DynamicColorEnabledRepository>()
        every { repository.dynamicColorEnabled() } returns flowOf(true)

        assertTrue(ObserveDynamicColorEnabledUseCase(repository)().first())
    }
}
