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
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.VehicleApproachStartReadoutType
import kurou.kodriver.domain.model.VehicleApproachSustainedReadoutType
import kurou.kodriver.domain.repository.LmuWindowsVehicleApproachPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createLmuWindowsVehicleApproachPreferencesRepository(
    repository: LmuWindowsVehicleApproachPreferencesRepository,
    initialSkipFirstLap: Boolean = true,
    initialStartReadoutType: VehicleApproachStartReadoutType = VehicleApproachStartReadoutType.CAR_LEFT_RIGHT,
    initialSustainedReadoutType: VehicleApproachSustainedReadoutType =
        VehicleApproachSustainedReadoutType.KEEP_LEFT_RIGHT,
): LmuWindowsVehicleApproachPreferencesRepository {
    val skipFirstLap = MutableStateFlow(initialSkipFirstLap)
    val startReadoutType = MutableStateFlow(initialStartReadoutType)
    val sustainedReadoutType = MutableStateFlow(initialSustainedReadoutType)
    every { repository.observeSkipFirstLap() } returns skipFirstLap
    coEvery { repository.saveSkipFirstLap(false) } answers { skipFirstLap.update { false } }
    every { repository.observeStartReadoutType() } returns startReadoutType
    coEvery { repository.saveStartReadoutType(VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH) } answers {
        startReadoutType.update { VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH }
    }
    every { repository.observeSustainedReadoutType() } returns sustainedReadoutType
    coEvery { repository.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED) } answers {
        sustainedReadoutType.update { VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED }
    }
    return repository
}

class LmuWindowsVehicleApproachPreferencesUseCasesTest {
    @MockK
    private lateinit var repository: LmuWindowsVehicleApproachPreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `observeSkipFirstLap はリポジトリの設定を返す`() =
        runBlocking {
            val repository =
                createLmuWindowsVehicleApproachPreferencesRepository(
                    repository,
                    initialSkipFirstLap = false,
                )
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals(false, useCases.observeSkipFirstLap().first())
            verify(exactly = 1) { repository.observeSkipFirstLap() }
            confirmVerified(repository)
        }

    @Test
    fun `saveSkipFirstLap はスキップ設定を保存する`() =
        runBlocking {
            val repository = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            useCases.saveSkipFirstLap(false)

            assertEquals(false, useCases.observeSkipFirstLap().first())
            coVerify(exactly = 1) { repository.saveSkipFirstLap(false) }
            verify(exactly = 1) { repository.observeSkipFirstLap() }
            confirmVerified(repository)
        }

    @Test
    fun `observeStartReadoutType はリポジトリの設定を返す`() =
        runBlocking {
            val repository =
                createLmuWindowsVehicleApproachPreferencesRepository(
                    repository,
                    initialStartReadoutType = VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                )
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals(
                VehicleApproachStartReadoutType.LEFT_RIGHT_APPROACH,
                useCases.observeStartReadoutType().first(),
            )
            verify(exactly = 1) { repository.observeStartReadoutType() }
            confirmVerified(repository)
        }

    @Test
    fun `saveStartReadoutType は接近開始時読み上げ種別を保存する`() =
        runBlocking {
            val repository = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

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

    @Test
    fun `observeSustainedReadoutType はリポジトリの設定を返す`() =
        runBlocking {
            val repository =
                createLmuWindowsVehicleApproachPreferencesRepository(
                    repository,
                    initialSustainedReadoutType = VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
                )
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            assertEquals(
                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
                useCases.observeSustainedReadoutType().first(),
            )
            verify(exactly = 1) { repository.observeSustainedReadoutType() }
            confirmVerified(repository)
        }

    @Test
    fun `saveSustainedReadoutType は接近継続時読み上げ種別を保存する`() =
        runBlocking {
            val repository = createLmuWindowsVehicleApproachPreferencesRepository(repository)
            val useCases = LmuWindowsVehicleApproachPreferencesUseCases(repository)

            useCases.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)

            assertEquals(
                VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED,
                useCases.observeSustainedReadoutType().first(),
            )
            coVerify(exactly = 1) {
                repository.saveSustainedReadoutType(VehicleApproachSustainedReadoutType.LEFT_RIGHT_SUSTAINED)
            }
            verify(exactly = 1) { repository.observeSustainedReadoutType() }
            confirmVerified(repository)
        }
}
