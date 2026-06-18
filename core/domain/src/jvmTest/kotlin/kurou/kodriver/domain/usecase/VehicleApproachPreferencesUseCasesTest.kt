package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleApproachPreferencesUseCasesTest {

    @Test
    fun `observeSkipFirstLap はリポジトリの設定を返す`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository(initialSkipFirstLap = false)
        val useCases = VehicleApproachPreferencesUseCases(repository)

        assertEquals(false, useCases.observeSkipFirstLap().first())
    }

    @Test
    fun `saveSkipFirstLap はスキップ設定を保存する`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository()
        val useCases = VehicleApproachPreferencesUseCases(repository)

        useCases.saveSkipFirstLap(false)

        assertEquals(false, useCases.observeSkipFirstLap().first())
    }

    @Test
    fun `observeStartReadoutEnabled はリポジトリの設定を返す`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository(initialStartReadoutEnabled = false)
        val useCases = VehicleApproachPreferencesUseCases(repository)

        assertEquals(false, useCases.observeStartReadoutEnabled().first())
    }

    @Test
    fun `saveStartReadoutEnabled は接近開始時読み上げ設定を保存する`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository()
        val useCases = VehicleApproachPreferencesUseCases(repository)

        useCases.saveStartReadoutEnabled(false)

        assertEquals(false, useCases.observeStartReadoutEnabled().first())
    }

    @Test
    fun `observeStartReadoutType はリポジトリの設定を返す`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository(
            initialStartReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
        )
        val useCases = VehicleApproachPreferencesUseCases(repository)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCases.observeStartReadoutType().first())
    }

    @Test
    fun `saveStartReadoutType は接近開始時読み上げ種別を保存する`() = runBlocking {
        val repository = FakeVehicleApproachPreferencesRepository()
        val useCases = VehicleApproachPreferencesUseCases(repository)

        useCases.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH)

        assertEquals(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH, useCases.observeStartReadoutType().first())
    }
}
