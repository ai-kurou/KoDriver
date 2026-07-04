package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveThemeModeUseCaseTest {

    @Test
    fun `テーマモードを保存できる`() = runBlocking {
        val repository = FakeThemePreferencesRepository()
        val saveUseCase = SaveThemeModeUseCase(repository)
        val observeUseCase = ObserveThemeModeUseCase(repository)

        saveUseCase(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, observeUseCase().first())
    }
}
