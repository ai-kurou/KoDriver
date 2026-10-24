package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.DismissedVolumes
import kurou.kodriver.domain.model.LowVolumeWarning
import kurou.kodriver.domain.repository.LowVolumeWarningDismissalRepository
import kotlin.test.Test

class DismissLowVolumeWarningUseCaseTest {
    private val repository: LowVolumeWarningDismissalRepository = mockk()

    @Test
    fun `端末音量だけを保存してアプリ音量の記憶を保持する`() =
        runTest {
            val current = DismissedVolumes(null, 70)
            every { repository.observeDismissedVolumes() } returns MutableStateFlow(current)
            coEvery { repository.saveDismissedVolumes(DismissedVolumes(20, 70)) } returns Unit

            DismissLowVolumeWarningUseCase(repository)(LowVolumeWarning(20, null))

            verify(exactly = 1) { repository.observeDismissedVolumes() }
            coVerify(exactly = 1) { repository.saveDismissedVolumes(DismissedVolumes(20, 70)) }
            confirmVerified(repository)
        }

    @Test
    fun `アプリ音量だけを保存して端末音量の記憶を保持する`() =
        runTest {
            val current = DismissedVolumes(15, null)
            every { repository.observeDismissedVolumes() } returns MutableStateFlow(current)
            coEvery { repository.saveDismissedVolumes(DismissedVolumes(15, 60)) } returns Unit

            DismissLowVolumeWarningUseCase(repository)(LowVolumeWarning(null, 60))

            verify(exactly = 1) { repository.observeDismissedVolumes() }
            coVerify(exactly = 1) { repository.saveDismissedVolumes(DismissedVolumes(15, 60)) }
            confirmVerified(repository)
        }

    @Test
    fun `両方の音量を保存する`() =
        runTest {
            every { repository.observeDismissedVolumes() } returns MutableStateFlow(DismissedVolumes())
            coEvery { repository.saveDismissedVolumes(DismissedVolumes(20, 60)) } returns Unit

            DismissLowVolumeWarningUseCase(repository)(LowVolumeWarning(20, 60))

            verify(exactly = 1) { repository.observeDismissedVolumes() }
            coVerify(exactly = 1) { repository.saveDismissedVolumes(DismissedVolumes(20, 60)) }
            confirmVerified(repository)
        }
}
