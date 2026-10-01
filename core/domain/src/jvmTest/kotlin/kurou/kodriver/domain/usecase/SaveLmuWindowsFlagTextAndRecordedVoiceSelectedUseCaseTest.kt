package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsFlagTextAndRecordedVoiceSelectedUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `文言と収録音声選択状態をまとめて保存する`() =
        runTest {
            SaveLmuWindowsFlagTextAndRecordedVoiceSelectedUseCase(repository)(
                LmuWindowsFlagReadoutTarget.BLUE_FLAG,
                "ブルー、譲って",
                false,
            )

            coVerify(exactly = 1) {
                repository.saveTextAndRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG, "ブルー、譲って", false)
            }
            confirmVerified(repository)
        }
}
