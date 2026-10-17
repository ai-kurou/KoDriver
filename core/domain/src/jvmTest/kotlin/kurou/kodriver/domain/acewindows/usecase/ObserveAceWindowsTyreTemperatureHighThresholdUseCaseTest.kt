package kurou.kodriver.domain.acewindows.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveAceWindowsTyreTemperatureHighThresholdUseCaseTest {
    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk()

    @Test
    fun `リポジトリの高温閾値を返す`() =
        runTest {
            val threshold = MutableStateFlow(Celsius(90))
            every { repository.observeHighThresholdCelsius() } returns threshold
            val useCase = ObserveAceWindowsTyreTemperatureHighThresholdUseCase(repository)

            assertEquals(Celsius(90), useCase().first())
            verify(exactly = 1) { repository.observeHighThresholdCelsius() }
            confirmVerified(repository)
        }
}
