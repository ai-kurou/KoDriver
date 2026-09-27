package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DEVICE_VOLUME_MAX
import kurou.kodriver.domain.model.DEVICE_VOLUME_MIN
import kurou.kodriver.domain.repository.DeviceVolumeRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SetDeviceVolumeUseCaseTest {
    private val repository: DeviceVolumeRepository = mockk(relaxUnitFun = true)

    @Test
    fun `0から100の値を設定できる`() =
        runTest {
            val useCase = SetDeviceVolumeUseCase(repository)

            useCase(DEVICE_VOLUME_MIN)
            useCase(50)
            useCase(DEVICE_VOLUME_MAX)

            coVerify(exactly = 1) { repository.setVolume(DEVICE_VOLUME_MIN) }
            coVerify(exactly = 1) { repository.setVolume(50) }
            coVerify(exactly = 1) { repository.setVolume(DEVICE_VOLUME_MAX) }
            confirmVerified(repository)
        }

    @Test
    fun `0未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SetDeviceVolumeUseCase(repository)(DEVICE_VOLUME_MIN - 1) }

            confirmVerified(repository)
        }

    @Test
    fun `100超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SetDeviceVolumeUseCase(repository)(DEVICE_VOLUME_MAX + 1) }

            confirmVerified(repository)
        }
}
