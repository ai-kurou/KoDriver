package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveOverlayTextSizeUseCaseTest {
    @MockK(relaxUnitFun = true)
    private lateinit var repository: OverlayTextSizePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `オーバーレイ文字サイズを保存できる`() =
        runTest {
            SaveOverlayTextSizeUseCase(repository)(OverlayTextSize.SMALL)

            coVerify(exactly = 1) { repository.saveOverlayTextSize(OverlayTextSize.SMALL) }
            confirmVerified(repository)
        }
}
