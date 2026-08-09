package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.model.ThemeMode
import kurou.kodriver.domain.repository.ThemePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveThemeModeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: ThemePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `テーマモードを保存できる`() =
        runTest {
            SaveThemeModeUseCase(repository)(ThemeMode.LIGHT)

            coVerify(exactly = 1) { repository.saveThemeMode(ThemeMode.LIGHT) }
            confirmVerified(repository)
        }
}
