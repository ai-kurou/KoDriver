package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveDeviceVolumeUseCaseTest {
    private val repository: DeviceVolumeRepository = mockk()

    @Test
    fun `Repositoryの音量Flowを返す`() =
        runTest {
            every { repository.observeVolume() } returns flowOf(42)
            val useCase = ObserveDeviceVolumeUseCase(repository)

            assertEquals(42, useCase().first())

            verify(exactly = 1) { repository.observeVolume() }
            confirmVerified(repository)
        }
}
