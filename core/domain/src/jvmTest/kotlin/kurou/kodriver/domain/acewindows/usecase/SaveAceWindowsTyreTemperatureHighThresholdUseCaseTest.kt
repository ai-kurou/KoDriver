package kurou.kodriver.domain.acewindows.usecase

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.repository.AceWindowsTyreTemperaturePreferencesRepository
import kurou.kodriver.domain.model.Celsius
import kotlin.test.Test

class SaveAceWindowsTyreTemperatureHighThresholdUseCaseTest {
    private val repository: AceWindowsTyreTemperaturePreferencesRepository = mockk(relaxUnitFun = true)

    @Test
    fun `高温閾値を保存する`() =
        runTest {
            SaveAceWindowsTyreTemperatureHighThresholdUseCase(repository)(Celsius(90))

            coVerify(exactly = 1) { repository.saveHighThresholdCelsius(Celsius(90)) }
            confirmVerified(repository)
        }
}
