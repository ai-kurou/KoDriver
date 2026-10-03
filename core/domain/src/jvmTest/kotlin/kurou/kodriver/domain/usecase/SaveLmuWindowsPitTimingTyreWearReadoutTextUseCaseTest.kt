package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsPitTimingPreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsPitTimingTyreWearReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(repository)("  ブルー、道を譲れ  ")

            coVerify(exactly = 1) { repository.saveTyreWearReadoutText("ブルー、道を譲れ") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(repository)("   ")

            coVerify(exactly = 1) { repository.saveTyreWearReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

            SaveLmuWindowsPitTimingTyreWearReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveTyreWearReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
