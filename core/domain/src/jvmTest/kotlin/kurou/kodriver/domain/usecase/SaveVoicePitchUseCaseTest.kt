package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VOICE_PITCH_MAX
import kurou.kodriver.domain.model.VOICE_PITCH_MIN
import kurou.kodriver.domain.repository.VoicePitchPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SaveVoicePitchUseCaseTest {
    private val repository: VoicePitchPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `0_5から2_0の値を保存できる`() =
        runTest {
            coEvery { repository.saveVoicePitch(VOICE_PITCH_MIN) } returns Unit
            coEvery { repository.saveVoicePitch(1.25f) } returns Unit
            coEvery { repository.saveVoicePitch(VOICE_PITCH_MAX) } returns Unit
            val useCase = SaveVoicePitchUseCase(repository)

            useCase(VOICE_PITCH_MIN)
            useCase(1.25f)
            useCase(VOICE_PITCH_MAX)

            coVerify(exactly = 1) { repository.saveVoicePitch(VOICE_PITCH_MIN) }
            coVerify(exactly = 1) { repository.saveVoicePitch(1.25f) }
            coVerify(exactly = 1) { repository.saveVoicePitch(VOICE_PITCH_MAX) }
            confirmVerified(repository)
        }

    @Test
    fun `0_5未満はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveVoicePitchUseCase(repository)(VOICE_PITCH_MIN - 0.1f) }

            coVerify(exactly = 0) { repository.saveVoicePitch(VOICE_PITCH_MIN - 0.1f) }
            confirmVerified(repository)
        }

    @Test
    fun `2_0超はIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveVoicePitchUseCase(repository)(VOICE_PITCH_MAX + 0.1f) }

            coVerify(exactly = 0) { repository.saveVoicePitch(VOICE_PITCH_MAX + 0.1f) }
            confirmVerified(repository)
        }

    @Test
    fun `NaNはIllegalArgumentExceptionをスローする`() =
        runTest {
            assertFailsWith<IllegalArgumentException> { SaveVoicePitchUseCase(repository)(Float.NaN) }

            coVerify(exactly = 0) { repository.saveVoicePitch(Float.NaN) }
            confirmVerified(repository)
        }
}
