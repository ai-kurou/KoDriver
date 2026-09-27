package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveThemeModeUseCaseTest {
    private val repository: ThemePreferencesRepository = mockk()

    @Test
    fun `テーマモードを監視できる`() =
        runTest {
            every { repository.observeThemeMode() } returns MutableStateFlow(ThemeMode.DARK)
            val useCase = ObserveThemeModeUseCase(repository)

            assertEquals(ThemeMode.DARK, useCase().first())
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }
}
