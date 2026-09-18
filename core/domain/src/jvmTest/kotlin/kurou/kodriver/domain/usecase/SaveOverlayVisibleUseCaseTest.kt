package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveOverlayVisibleUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: OverlayVisiblePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `表示・非表示のどちらも保存できる`() =
        runTest {
            val useCase = SaveOverlayVisibleUseCase(repository)

            useCase(true)
            useCase(false)

            coVerify(exactly = 1) { repository.saveOverlayVisible(true) }
            coVerify(exactly = 1) { repository.saveOverlayVisible(false) }
            confirmVerified(repository)
        }
}
