package kurou.kodriver.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsWeatherForecast
import kurou.kodriver.domain.model.LmuWindowsWeatherForecastNode
import kurou.kodriver.domain.model.LmuWindowsWeatherForecastNodeData
import kurou.kodriver.domain.model.LmuWindowsWeatherSessionType
import kurou.kodriver.domain.repository.LmuWindowsWeatherForecastRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetLmuWindowsWeatherForecastUseCaseTest {
    @MockK
    private lateinit var repo: LmuWindowsWeatherForecastRepository

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `invokeはリポジトリのweatherForecastsを返す`() =
        runTest {
            val expected = listOf(fakeWeatherForecast(LmuWindowsWeatherSessionType.RACE))
            coEvery { repo.weatherForecasts() } returns expected
            val useCase = GetLmuWindowsWeatherForecastUseCase(repo)

            val result = useCase()

            assertEquals(expected, result)
            coVerify(exactly = 1) { repo.weatherForecasts() }
            confirmVerified(repo)
        }

    @Test
    fun `invokeは空リストをそのまま返す`() =
        runTest {
            coEvery { repo.weatherForecasts() } returns emptyList()
            val useCase = GetLmuWindowsWeatherForecastUseCase(repo)

            val result = useCase()

            assertTrue(result.isEmpty())
            coVerify(exactly = 1) { repo.weatherForecasts() }
            confirmVerified(repo)
        }

    private fun fakeWeatherForecast(sessionType: LmuWindowsWeatherSessionType) =
        LmuWindowsWeatherForecast(
            sessionType = sessionType,
            nodes =
                listOf(
                    LmuWindowsWeatherForecastNodeData(
                        node = LmuWindowsWeatherForecastNode.START,
                        skyIndex = 0,
                        temperatureCelsius = 23,
                        rainChancePercent = 0,
                        humidityPercent = 75,
                        windDirectionIndex = 1,
                        windSpeedKph = 18.0,
                    ),
                ),
        )
}
