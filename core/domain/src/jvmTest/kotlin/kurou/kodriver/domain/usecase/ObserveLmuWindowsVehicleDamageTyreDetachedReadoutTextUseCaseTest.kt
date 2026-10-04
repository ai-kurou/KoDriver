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

class ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk()

    @Test
    fun `タイヤ脱落読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeTyreDetachedReadoutText() } returns MutableStateFlow("タイヤ脱落")
            val useCase = ObserveLmuWindowsVehicleDamageTyreDetachedReadoutTextUseCase(repository)

            assertEquals("タイヤ脱落", useCase().first())
            verify(exactly = 1) { repository.observeTyreDetachedReadoutText() }
            confirmVerified(repository)
        }
}
