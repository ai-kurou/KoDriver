package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.Gt7Ps5Repository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveGt7Ps5VehicleClassUseCaseTest {
    @MockK
    private lateinit var repo: Gt7Ps5Repository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのtelemetryStreamからcarCategoryだけを取り出す`() =
        runTest {
            val data = fakeGt7Ps5TelemetryData(carCategory = "GR3")
            every { repo.telemetryStream() } returns flowOf(data)
            val useCase = ObserveGt7Ps5VehicleClassUseCase(repo)

            val result = useCase().first()

            assertEquals("GR3", result.name)
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }

    @Test
    fun `carCategoryが空文字列の場合は空文字列を返す`() =
        runTest {
            val data = fakeGt7Ps5TelemetryData(carCategory = "")
            every { repo.telemetryStream() } returns flowOf(data)
            val useCase = ObserveGt7Ps5VehicleClassUseCase(repo)

            val result = useCase().first()

            assertEquals("", result.name)
            verify(exactly = 1) { repo.telemetryStream() }
            confirmVerified(repo)
        }
}
