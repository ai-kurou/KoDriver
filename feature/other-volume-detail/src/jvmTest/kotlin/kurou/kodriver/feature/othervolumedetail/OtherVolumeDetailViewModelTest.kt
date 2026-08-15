@file:Suppress("FunctionNaming")

package kurou.kodriver.feature.othervolumedetail

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.repository.SoundVolumePreferencesRepository
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kurou.kodriver.domain.usecase.ObserveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SaveSoundVolumeUseCase
import kurou.kodriver.domain.usecase.SetDeviceVolumeUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OtherVolumeDetailViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @MockK
    private lateinit var soundVolumeRepository: SoundVolumePreferencesRepository

    @MockK
    private lateinit var deviceVolumeRepository: DeviceVolumeRepository

    private val volumeFlow = MutableStateFlow(80)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        OtherVolumeDetailViewModel(
            observeSoundVolume = ObserveSoundVolumeUseCase(soundVolumeRepository),
            saveSoundVolume = SaveSoundVolumeUseCase(soundVolumeRepository),
            getDeviceVolume = GetDeviceVolumeUseCase(deviceVolumeRepository),
            setDeviceVolume = SetDeviceVolumeUseCase(deviceVolumeRepository),
        )

    @Test
    fun `保存済みの音量と端末のマスター音量をUiStateで返す`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val viewModel = createViewModel()

            assertEquals(OtherVolumeDetailUiState(volume = 80, deviceVolume = 60), viewModel.uiState.first())
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 1) { deviceVolumeRepository.getVolume() }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `音量を変更するとUiStateが更新される`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            coEvery { soundVolumeRepository.saveVolume(40) } answers { volumeFlow.update { 40 } }
            val viewModel = createViewModel()

            viewModel.onVolumeChanged(40)

            assertEquals(OtherVolumeDetailUiState(volume = 40, deviceVolume = 60), viewModel.uiState.first())
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 1) { deviceVolumeRepository.getVolume() }
            coVerify(exactly = 1) { soundVolumeRepository.saveVolume(40) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `端末のマスター音量を変更すると設定後に再取得してUiStateへ反映する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returnsMany listOf(60, 30)
            coEvery { deviceVolumeRepository.setVolume(30) } returns Unit
            val viewModel = createViewModel()
            assertEquals(60, viewModel.uiState.first().deviceVolume)

            viewModel.onDeviceVolumeChanged(30)

            assertEquals(OtherVolumeDetailUiState(volume = 80, deviceVolume = 30), viewModel.uiState.first())
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 2) { deviceVolumeRepository.getVolume() }
            coVerify(exactly = 1) { deviceVolumeRepository.setVolume(30) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `端末のマスター音量を連続して変更すると書き込みが直列に実行される`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            val callOrder = mutableListOf<String>()
            coEvery { deviceVolumeRepository.setVolume(20) } coAnswers {
                callOrder.add("start:20")
                delay(100)
                callOrder.add("end:20")
            }
            coEvery { deviceVolumeRepository.setVolume(80) } coAnswers {
                callOrder.add("start:80")
            }
            val viewModel = createViewModel()

            viewModel.onDeviceVolumeChanged(20)
            viewModel.onDeviceVolumeChanged(80)
            advanceUntilIdle()

            assertEquals(listOf("start:20", "end:20", "start:80"), callOrder)
            verify(exactly = 1) { soundVolumeRepository.volume() }
            coVerify(exactly = 1) { deviceVolumeRepository.setVolume(20) }
            coVerify(exactly = 1) { deviceVolumeRepository.setVolume(80) }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
        }

    @Test
    fun `detailPane表示中は一定間隔で端末のマスター音量を再取得する`() =
        runTest {
            every { soundVolumeRepository.volume() } returns volumeFlow
            coEvery { deviceVolumeRepository.getVolume() } returns 60
            val viewModel = createViewModel()
            val job = viewModel.uiState.launchIn(this)
            runCurrent()

            advanceTimeBy(500)
            runCurrent()
            advanceTimeBy(500)
            runCurrent()

            coVerify(exactly = 3) { deviceVolumeRepository.getVolume() }
            verify(exactly = 1) { soundVolumeRepository.volume() }
            confirmVerified(soundVolumeRepository, deviceVolumeRepository)
            job.cancel()
        }
}
