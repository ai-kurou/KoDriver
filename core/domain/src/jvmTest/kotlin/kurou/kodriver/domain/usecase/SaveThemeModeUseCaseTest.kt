package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.Test

class SaveThemeModeUseCaseTest {

    @Test
    fun `テーマモードを保存できる`() = runBlocking {
        val repository = mockk<ThemePreferencesRepository>(relaxUnitFun = true)

        SaveThemeModeUseCase(repository)(ThemeMode.LIGHT)

        coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.LIGHT) }
        confirmVerified(repository)
    }
}
