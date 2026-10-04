package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk()

    @Test
    fun `部品脱落読み上げ文言を監視できる`() =
        runTest {
            every { repository.observePartDetachedReadoutText() } returns MutableStateFlow("部品脱落")
            val useCase = ObserveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository)

            assertEquals("部品脱落", useCase().first())
            verify(exactly = 1) { repository.observePartDetachedReadoutText() }
            confirmVerified(repository)
        }
}
