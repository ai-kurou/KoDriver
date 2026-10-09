package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VOICE_SPEED_MAX
import kurou.kodriver.domain.model.VOICE_SPEED_MIN
import kurou.kodriver.domain.repository.VoiceSpeedPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveVoiceSpeedUseCaseTest {
    private val repository: VoiceSpeedPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `0_5から2_0の値を保存できる`() =
        runTest {
            val useCase = SaveVoiceSpeedUseCase(repository)

            useCase(VOICE_SPEED_MIN)
            useCase(1.25f)
            useCase(VOICE_SPEED_MAX)

            coVerify(exactly = 1) { repository.saveVoiceSpeed(VOICE_SPEED_MIN) }
            coVerify(exactly = 1) { repository.saveVoiceSpeed(1.25f) }
            coVerify(exactly = 1) { repository.saveVoiceSpeed(VOICE_SPEED_MAX) }
            confirmVerified(repository)
        }

    @Test
    fun `0_5未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveVoiceSpeedUseCase(repository)(VOICE_SPEED_MIN - 0.1f) }

            coVerify(exactly = 0) { repository.saveVoiceSpeed(VOICE_SPEED_MIN - 0.1f) }
            confirmVerified(repository)
        }

    @Test
    fun `2_0超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveVoiceSpeedUseCase(repository)(VOICE_SPEED_MAX + 0.1f) }

            coVerify(exactly = 0) { repository.saveVoiceSpeed(VOICE_SPEED_MAX + 0.1f) }
            confirmVerified(repository)
        }

    @Test
    fun `NaNはIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveVoiceSpeedUseCase(repository)(Float.NaN) }

            coVerify(exactly = 0) { repository.saveVoiceSpeed(Float.NaN) }
            confirmVerified(repository)
        }
}
