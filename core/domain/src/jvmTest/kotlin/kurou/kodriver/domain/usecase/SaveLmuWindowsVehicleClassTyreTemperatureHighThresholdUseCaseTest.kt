package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.repository.LmuWindowsVehicleClassTyreTemperaturePreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test

class SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCaseTest {
    // saveHighThresholdCelsius は戻り値 Unit の suspend 関数のため relaxUnitFun でスタブ不要にし、
    // coEvery を省略して coVerify のみで呼び出しを検証する
    @MockK(relaxUnitFun = true)
    private lateinit var repository: LmuWindowsVehicleClassTyreTemperaturePreferencesRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `車両クラスごとに任意の値を保存できる`() =
        runTest {
            val useCase = SaveLmuWindowsVehicleClassTyreTemperatureHighThresholdUseCase(repository)

            useCase(LmuWindowsVehicleClassData.Gte, Celsius(100))
            useCase(LmuWindowsVehicleClassData.Gt3, Celsius(75))

            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, Celsius(100))
            }
            coVerify(exactly = 1) {
                repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gt3, Celsius(75))
            }
            confirmVerified(repository)
        }
}
