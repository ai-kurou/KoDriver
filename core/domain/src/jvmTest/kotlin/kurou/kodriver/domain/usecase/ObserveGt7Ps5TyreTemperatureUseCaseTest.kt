package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.CelsiusReading
import kurou.kodriver.domain.model.Gt7Ps5TyreTemperatureData
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5TyreTemperatureUseCaseTest {
    @MockK
    private lateinit var repo: Gt7Ps5Repository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのtelemetryStreamからtyreTemperatureだけを取り出す`() =
        runTest {
            val tyreTemperature =
                Gt7Ps5TyreTemperatureData(
                    CelsiusReading(80f),
                    CelsiusReading(82f),
                    CelsiusReading(78f),
                    CelsiusReading(79f),
                )
            val data = fakeGt7Ps5TelemetryData(tyreTemperature = tyreTemperature)
            every { repo.telemetryStream() } returns flowOf(data)
            val useCase = ObserveGt7Ps5TyreTemperatureUseCase(repo)

            val result = useCase().first()

            assertEquals(tyreTemperature, result)
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }

    @Test
    fun `タイヤ温度が全て0の場合はそのまま0を返す`() =
        runTest {
            val tyreTemperature =
                Gt7Ps5TyreTemperatureData(
                    CelsiusReading(0f),
                    CelsiusReading(0f),
                    CelsiusReading(0f),
                    CelsiusReading(0f),
                )
            val data = fakeGt7Ps5TelemetryData(tyreTemperature = tyreTemperature)
            every { repo.telemetryStream() } returns flowOf(data)
            val useCase = ObserveGt7Ps5TyreTemperatureUseCase(repo)

            val result = useCase().first()

            assertEquals(tyreTemperature, result)
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }
}
