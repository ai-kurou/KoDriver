package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kotlin.test.Test

class SaveAndroidAudioUsageUseCaseTest {
    private val repository: AndroidAudioUsagePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `Android音声用途を保存できる`() =
        runTest {
            SaveAndroidAudioUsageUseCase(repository)(AndroidAudioUsage.ASSISTANT)

            coVerify(exactly = 1) { repository.saveAndroidAudioUsage(AndroidAudioUsage.ASSISTANT) }
            confirmVerified(repository)
        }
}
