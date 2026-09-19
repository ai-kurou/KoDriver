package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDeviceVolumeUseCaseTest {
    private val repository: DeviceVolumeRepository = mockk()

    @Test
    fun `Repositoryの値をそのまま返す`() =
        runTest {
            coEvery { repository.getVolume() } returns 42
            val useCase = GetDeviceVolumeUseCase(repository)

            val result = useCase()

            assertEquals(42, result)
            coVerify(exactly = 1) { repository.getVolume() }
            confirmVerified(repository)
        }
}
