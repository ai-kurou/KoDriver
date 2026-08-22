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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.AceWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createAceWindowsVehicleApproachPreferencesRepository(
    repository: AceWindowsVehicleApproachPreferencesRepository,
    initialStartReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
): AceWindowsVehicleApproachPreferencesRepository {
    val startReadoutType = MutableStateFlow(initialStartReadoutType)
    every { repository.observeStartReadoutType() } returns startReadoutType
    coEvery { repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH) } answers {
        startReadoutType.update { VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH }
    }
    return repository
}

class AceWindowsVehicleApproachPreferencesUseCasesTest {
    @MockK
    private lateinit var repository: AceWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `observeStartReadoutType はリポジトリの設定を返す`() =
        runTest {
            val repository =
                createAceWindowsVehicleApproachPreferencesRepository(
                    repository,
                    initialStartReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                )
            val useCases = AceWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                useCases.observeStartReadoutType().first(),
            )
            verify(exactly = 1) { repository.observeStartReadoutType() }
            confirmVerified(repository)
        }

    @Test
    fun `saveStartReadoutType は接近開始時読み上げ種別を保存する`() =
        runTest {
            val repository = createAceWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = AceWindowsVehicleApproachPreferencesUseCases(repository)

            useCases.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

            assertEquals(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                useCases.observeStartReadoutType().first(),
            )
            coVerify(exactly = 1) {
                repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)
            }
            verify(exactly = 1) { repository.observeStartReadoutType() }
            confirmVerified(repository)
        }
}
