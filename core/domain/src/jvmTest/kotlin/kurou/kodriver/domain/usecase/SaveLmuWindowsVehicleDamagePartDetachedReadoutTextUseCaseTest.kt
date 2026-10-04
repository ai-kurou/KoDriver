package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.repository.LmuWindowsVehicleDamagePreferencesRepository
import kotlin.test.Test

class SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCaseTest {
    private val repository: LmuWindowsVehicleDamagePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `前後の空白を除去して保存する`() =
        runTest {
            SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository)("  部品脱落  ")

            coVerify(exactly = 1) { repository.savePartDetachedReadoutText("部品脱落") }
            confirmVerified(repository)
        }

    @Test
    fun `空白のみの入力は空文字として保存する`() =
        runTest {
            SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository)(" \t\n ")

            coVerify(exactly = 1) { repository.savePartDetachedReadoutText("") }
            confirmVerified(repository)
        }

    @Test
    fun `最大文字数を超える入力は切り詰めて保存する`() =
        runTest {
            val text = "  " + "あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH + 5) + "  "

            SaveLmuWindowsVehicleDamagePartDetachedReadoutTextUseCase(repository)(text)

            coVerify(exactly = 1) {
                repository.savePartDetachedReadoutText("あ".repeat(READOUT_CUSTOM_TEXT_MAX_LENGTH))
            }
            confirmVerified(repository)
        }
}
