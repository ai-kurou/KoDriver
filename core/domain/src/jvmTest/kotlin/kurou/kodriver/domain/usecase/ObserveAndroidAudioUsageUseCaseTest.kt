package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.AndroidAudioUsage
import kurou.kodriver.domain.repository.AndroidAudioUsagePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAndroidAudioUsageUseCaseTest {
    private val repository: AndroidAudioUsagePreferencesRepository = mockk()

    @Test
    fun `Android音声用途を監視できる`() =
        runTest {
            every { repository.observeAndroidAudioUsage() } returns MutableStateFlow(AndroidAudioUsage.ALARM)
            val useCase = ObserveAndroidAudioUsageUseCase(repository)

            assertEquals(AndroidAudioUsage.ALARM, useCase().first())
            verify(exactly = 1) { repository.observeAndroidAudioUsage() }
            confirmVerified(repository)
        }
}
