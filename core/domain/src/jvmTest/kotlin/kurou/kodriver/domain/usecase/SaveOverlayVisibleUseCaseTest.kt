package kurou.kodriver.domain.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.OverlayVisiblePreferencesRepository
import kotlin.test.Test

class SaveOverlayVisibleUseCaseTest {
    private val repository: OverlayVisiblePreferencesRepository = mockk(relaxUnitFun = true)

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
