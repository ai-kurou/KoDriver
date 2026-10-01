package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsFlagReadoutTarget
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsFlagRecordedVoiceSelectedUseCaseTest {
    private val repository: LmuWindowsFlagReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `指定したフラッグの収録音声選択状態を保存する`() =
        runTest {
            SaveLmuWindowsFlagRecordedVoiceSelectedUseCase(repository)(LmuWindowsFlagReadoutTarget.BLUE_FLAG, true)

            coVerify(exactly = 1) { repository.saveRecordedVoiceSelected(LmuWindowsFlagReadoutTarget.BLUE_FLAG, true) }
            confirmVerified(repository)
        }
}
