package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kurou.kodriver.domain.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveThemeModeUseCaseTest {
    @MockK
    private lateinit var repository: ThemePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `テーマモードを監視できる`() =
        runBlocking {
            every { repository.observeThemeMode() } returns MutableStateFlow(ThemeMode.DARK)
            val useCase = ObserveThemeModeUseCase(repository)

            assertEquals(ThemeMode.DARK, useCase().first())
            verify(exactly = 1) { repository.observeThemeMode() }
            confirmVerified(repository)
        }
}
