package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `初期値を返す・保存済みの値を返す`() =
        runTest {
            val state = MutableStateFlow<LmuWindowsVehicleClassData>(LmuWindowsVehicleClassData.Hypercar)
            every { repo.observeSelectedVehicleClass() } returns state
            coEvery { repo.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) } answers {
                state.value = LmuWindowsVehicleClassData.Gte
            }
            val useCase = ObserveLmuWindowsVehicleClassTyreTemperatureSelectionUseCase(repo)

            assertEquals(LmuWindowsVehicleClassData.Hypercar, useCase().first())

            repo.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte)
            assertEquals(LmuWindowsVehicleClassData.Gte, useCase().first())

            verify(exactly = 2) { repo.observeSelectedVehicleClass() }
            coVerify(exactly = 1) { repo.saveSelectedVehicleClass(LmuWindowsVehicleClassData.Gte) }
            confirmVerified(repo)
        }
}
