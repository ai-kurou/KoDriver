package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFlagReadoutTextKey
import kurou.kodriver.domain.acewindows.repository.AceWindowsFlagReadoutTextPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsFlagReadoutTextUseCasesTest {
    private val repository = mockk<AceWindowsFlagReadoutTextPreferencesRepository>(relaxUnitFun = true)

    @Test
    fun `ObserveUseCaseは全フラッグのキーでRepositoryの値をそのまま流す`() =
        runTest {
            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                every { repository.observeText(key) } returns flowOf("文言:${key.name}")
            }

            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                assertEquals("文言:${key.name}", ObserveAceWindowsFlagReadoutTextUseCase(repository)(key).first())
            }

            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                verify(exactly = 1) { repository.observeText(key) }
            }
            confirmVerified(repository)
        }

    @Test
    fun `SaveUseCaseは全フラッグのキーで正規化した文言をRepositoryに保存する`() =
        runTest {
            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                SaveAceWindowsFlagReadoutTextUseCase(repository)(key, "  文言:${key.name}  ")
            }

            AceWindowsFlagReadoutTextKey.entries.forEach { key ->
                coVerify(exactly = 1) { repository.saveText(key, "文言:${key.name}") }
            }
            confirmVerified(repository)
        }

    @Test
    fun `SaveUseCaseは空白のみの入力を空文字として保存する`() =
        runTest {
            SaveAceWindowsFlagReadoutTextUseCase(repository)(AceWindowsFlagReadoutTextKey.BLUE, "   ")

            coVerify(exactly = 1) { repository.saveText(AceWindowsFlagReadoutTextKey.BLUE, "") }
            confirmVerified(repository)
        }
}
