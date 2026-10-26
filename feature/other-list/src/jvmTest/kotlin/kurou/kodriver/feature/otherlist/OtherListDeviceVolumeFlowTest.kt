package kurou.kodriver.feature.otherlist

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kurou.kodriver.domain.usecase.GetDeviceVolumeUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class OtherListDeviceVolumeFlowTest {
    private val repository: DeviceVolumeRepository = mockk()

    @Test
    fun `購読開始時に即時取得し2秒間隔で値を流す`() =
        runTest {
            coEvery { repository.getVolume() } returnsMany listOf(40, 60)
            val flow = OtherListDeviceVolumeFlow(GetDeviceVolumeUseCase(repository))
            val values = mutableListOf<Int>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { flow.volume.take(2).collect(values::add) }

            runCurrent()
            assertEquals(listOf(40), values)
            advanceTimeBy(1_999)
            runCurrent()
            assertEquals(listOf(40), values)
            advanceTimeBy(1)
            runCurrent()
            job.join()
            assertEquals(listOf(40, 60), values)
            coVerify(exactly = 2) { repository.getVolume() }
            confirmVerified(repository)
        }

    @Test
    fun `取得失敗時は初回に最小値を流し直前の値を維持して再試行する`() =
        runTest {
            var attempt = 0
            coEvery { repository.getVolume() } coAnswers {
                when (attempt++) {
                    0 -> throw IllegalStateException("no audio endpoint")
                    1 -> 60
                    else -> throw IllegalStateException("device removed")
                }
            }
            val flow = OtherListDeviceVolumeFlow(GetDeviceVolumeUseCase(repository))
            val values = mutableListOf<Int>()
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { flow.volume.take(3).collect(values::add) }

            runCurrent()
            assertEquals(listOf(DEVICE_VOLUME_MIN), values)
            advanceTimeBy(2_000)
            runCurrent()
            assertEquals(listOf(DEVICE_VOLUME_MIN, 60), values)
            advanceTimeBy(2_000)
            runCurrent()
            job.join()
            assertEquals(listOf(DEVICE_VOLUME_MIN, 60, 60), values)
            coVerify(exactly = 3) { repository.getVolume() }
            confirmVerified(repository)
        }

    @Test
    fun `CancellationExceptionを伝播する`() =
        runTest {
            val cancellation = CancellationException("cancelled")
            coEvery { repository.getVolume() } throws cancellation
            val flow = OtherListDeviceVolumeFlow(GetDeviceVolumeUseCase(repository))

            val thrown = assertFailsWith<CancellationException> { flow.volume.collect { } }

            assertEquals(cancellation, thrown)
            coVerify(exactly = 1) { repository.getVolume() }
            confirmVerified(repository)
        }

    @Test
    fun `購読キャンセル後はポーリングを停止する`() =
        runTest {
            coEvery { repository.getVolume() } returns 40
            val flow = OtherListDeviceVolumeFlow(GetDeviceVolumeUseCase(repository))
            val job = launch(UnconfinedTestDispatcher(testScheduler)) { flow.volume.collect { } }

            runCurrent()
            assertFalse(job.isCompleted)
            job.cancelAndJoin()
            advanceTimeBy(4_000)
            runCurrent()

            coVerify(exactly = 1) { repository.getVolume() }
            confirmVerified(repository)
        }
}
