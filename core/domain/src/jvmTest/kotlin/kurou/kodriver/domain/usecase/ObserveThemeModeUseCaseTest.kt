package kurou.kodriver.domain.usecase

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveThemeModeUseCaseTest {

    @Test
    fun `テーマモードを監視できる`() = runBlocking {
        val repository = mockk<ThemePreferencesRepository>()
        every { repository.observeThemeMode() } returns MutableStateFlow(ThemeMode.DARK)
        val useCase = ObserveThemeModeUseCase(repository)

        assertEquals(ThemeMode.DARK, useCase().first())
    }
}
