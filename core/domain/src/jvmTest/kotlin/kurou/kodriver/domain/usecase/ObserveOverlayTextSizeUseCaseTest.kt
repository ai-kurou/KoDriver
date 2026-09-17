package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.domain.repository.OverlayTextSizePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveOverlayTextSizeUseCaseTest {
    @MockK
    private lateinit var repository: OverlayTextSizePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `オーバーレイ文字サイズを監視できる`() =
        runTest {
            every { repository.observeOverlayTextSize() } returns MutableStateFlow(OverlayTextSize.LARGE)
            val useCase = ObserveOverlayTextSizeUseCase(repository)

            assertEquals(OverlayTextSize.LARGE, useCase().first())
            verify(exactly = 1) { repository.observeOverlayTextSize() }
            confirmVerified(repository)
        }
}
