package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.ManualReadoutPlayButtonLayoutEnabledPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class ObserveManualReadoutPlayButtonLayoutEnabledUseCaseTest {
    private val repository: ManualReadoutPlayButtonLayoutEnabledPreferencesRepository = mockk()

    @Test
    fun `保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow(true)
            every { repository.observeManualReadoutPlayButtonLayoutEnabled() } returns state

            val useCase = ObserveManualReadoutPlayButtonLayoutEnabledUseCase(repository)

            assertTrue(useCase().first())

            verify(exactly = 1) { repository.observeManualReadoutPlayButtonLayoutEnabled() }
            confirmVerified(repository)
        }
}
