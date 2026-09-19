package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.Test

class SaveThemeModeUseCaseTest {
    private val repository: ThemePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `テーマモードを保存できる`() =
        runTest {
            SaveThemeModeUseCase(repository)(ThemeMode.LIGHT)

            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.LIGHT) }
            confirmVerified(repository)
        }
}
