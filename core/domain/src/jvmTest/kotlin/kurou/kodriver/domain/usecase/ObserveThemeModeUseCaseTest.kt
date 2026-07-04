package kurou.kodriver.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveThemeModeUseCaseTest {

    @Test
    fun `テーマモードを監視できる`() = runBlocking {
        val repository = FakeThemePreferencesRepository(initialThemeMode = ThemeMode.DARK)
        val useCase = ObserveThemeModeUseCase(repository)

        assertEquals(ThemeMode.DARK, useCase().first())
    }
}
