package kurou.kodriver.core.lmuwindowsrestapidata.datasource

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherForecastResponseDto

internal class LmuWindowsRestApiWeatherDataSource(
    private val client: HttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL,
) {
    suspend fun fetchWeatherForecast(): WeatherForecastResponseDto = client.get("$baseUrl$WEATHER_PATH").body()

    companion object {
        private const val DEFAULT_BASE_URL = "http://localhost:6397"
        private const val WEATHER_PATH = "/rest/sessions/weather"
    }
}
