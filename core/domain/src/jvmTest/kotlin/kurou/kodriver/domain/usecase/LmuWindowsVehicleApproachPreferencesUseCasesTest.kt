package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    initialSkipFirstLap: Boolean = true,
    initialStartReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
): LmuWindowsVehicleApproachPreferencesRepository {
    val repository = mockk<LmuWindowsVehicleApproachPreferencesRepository>()
    val skipFirstLap = MutableStateFlow(initialSkipFirstLap)
    val startReadoutType = MutableStateFlow(initialStartReadoutType)
    every { repository.observeSkipFirstLap() } returns skipFirstLap
    coEvery { repository.saveSkipFirstLap(any()) } answers { skipFirstLap.update { firstArg() } }
    every { repository.observeStartReadoutType() } returns startReadoutType
    coEvery { repository.saveStartReadoutType(any()) } answers { startReadoutType.update { firstArg() } }
    return repository
}

class LmuWindowsVehicleApproachPreferencesUseCasesTest {

    @Test
    fun `observeSkipFirstLap はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository(initialSkipFirstLap = false)
        val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

        assertEquals(false, useCases.observeSkipFirstLap().first())
    }

    @Test
    fun `saveSkipFirstLap はスキップ設定を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

        useCases.saveSkipFirstLap(false)

        assertEquals(false, useCases.observeSkipFirstLap().first())
    }

    @Test
    fun `observeStartReadoutType はリポジトリの設定を返す`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository(
            initialStartReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
        )
        val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCases.observeStartReadoutType().first())
    }

    @Test
    fun `saveStartReadoutType は接近開始時読み上げ種別を保存する`() = runBlocking {
        val repository = createLmuWindowsVehicleApproachPreferencesRepository()
        val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

        useCases.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCases.observeStartReadoutType().first())
    }
}
