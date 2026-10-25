package kurou.kodriver.domain.lmuwindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.repository.LmuWindowsPitTimingReadoutTextPreferencesRepository
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kotlin.test.Test

class SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCaseTest {
    private val repository: LmuWindowsPitTimingReadoutTextPreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(repository)("  ブルー、道を譲れ  ")

            coVerify(exactly = 1) { repository.saveFuelImminentReadoutText("ブルー、道を譲れ") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は未設定として保存する`() =
        runTest {
            SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(repository)("   ")

            coVerify(exactly = 1) { repository.saveFuelImminentReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5)

            SaveLmuWindowsPitTimingFuelImminentReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.saveFuelImminentReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
