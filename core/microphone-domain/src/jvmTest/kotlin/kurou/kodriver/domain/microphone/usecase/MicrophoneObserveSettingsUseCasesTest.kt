package kurou.kodriver.domain.microphone.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.microphone.repository.MicrophoneCutQuietSoundPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class MicrophoneObserveSettingsUseCasesTest {
    @Test
    fun `Observe系UseCaseはRepositoryの監視Flowの値をそのまま返す`() {
        assertObserves<MicrophoneCutQuietSoundPreferencesRepository, Int>(
            "ObserveMicrophoneCutQuietSoundLevelUseCase",
            4,
            observe = { observeMicrophoneCutQuietSoundLevel() },
            useCase = { ObserveMicrophoneCutQuietSoundLevelUseCase(it)() },
        )
        assertObserves<MicrophoneCutQuietSoundPreferencesRepository, Boolean>(
            "ObserveMicrophoneCutQuietSoundInCommandUseCase",
            true,
            observe = { observeMicrophoneCutQuietSoundInCommand() },
            useCase = { ObserveMicrophoneCutQuietSoundInCommandUseCase(it)() },
        )
    }

    private inline fun <reified R : Any, T> assertObserves(
        useCaseName: String,
        expected: T,
        crossinline observe: R.() -> Flow<T>,
        crossinline useCase: (R) -> Flow<T>,
    ) {
        val repository = mockk<R>()
        every { repository.observe() } returns flowOf(expected)
        try {
            runTest {
                assertEquals(expected, useCase(repository).first())
            }
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        } catch (e: AssertionError) {
            throw AssertionError("$useCaseName: ${e.message}", e)
        }
    }
}
