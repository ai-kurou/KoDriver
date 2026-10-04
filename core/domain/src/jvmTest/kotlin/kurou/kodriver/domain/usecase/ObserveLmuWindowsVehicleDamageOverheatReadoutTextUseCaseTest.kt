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

class ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk()

    @Test
    fun `オーバーヒート読み上げ文言を監視できる`() =
        runTest {
            every { repository.observeOverheatReadoutText() } returns MutableStateFlow("オーバーヒート")
            val useCase = ObserveLmuWindowsVehicleDamageOverheatReadoutTextUseCase(repository)

            assertEquals("オーバーヒート", useCase().first())
            verify(exactly = 1) { repository.observeOverheatReadoutText() }
            confirmVerified(repository)
        }
}
