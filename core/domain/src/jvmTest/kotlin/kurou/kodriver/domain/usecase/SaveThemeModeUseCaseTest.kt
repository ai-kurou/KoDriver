package kurou.kodriver.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private fun createThemePreferencesRepository(
    initial: ThemeMode = ThemeMode.SYSTEM,
): ThemePreferencesRepository {
    val repository = mockk<ThemePreferencesRepository>()
    val state = MutableStateFlow(initial)
    every { repository.observeThemeMode() } returns state
    coEvery { repository.saveThemeMode(any()) } answers {
        state.update { firstArg() }
    }
    return repository
}

class SaveThemeModeUseCaseTest {

    @Test
    fun `テーマモードを保存できる`() = runBlocking {
        val repository = createThemePreferencesRepository()
        val saveUseCase = SaveThemeModeUseCase(repository)
        val observeUseCase = ObserveThemeModeUseCase(repository)

        saveUseCase(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, observeUseCase().first())
    }
}
