package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.HotkeyRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveReadoutHotkeyUseCaseTest {
    private val repository: HotkeyRepository = mockk()

    @Test
    fun `押下を順番通りに通知する`() =
        runTest {
            every { repository.hotkeyPressStream() } returns flowOf(Unit, Unit)

            assertEquals(listOf(Unit, Unit), ObserveReadoutHotkeyUseCase(repository)().toList())

            verify(exactly = 1) { repository.hotkeyPressStream() }
            confirmVerified(repository)
        }

    @Test
    fun `通知のないフローは空のまま返す`() =
        runTest {
            every { repository.hotkeyPressStream() } returns emptyFlow()

            assertEquals(emptyList(), ObserveReadoutHotkeyUseCase(repository)().toList())

            verify(exactly = 1) { repository.hotkeyPressStream() }
            confirmVerified(repository)
        }
}
