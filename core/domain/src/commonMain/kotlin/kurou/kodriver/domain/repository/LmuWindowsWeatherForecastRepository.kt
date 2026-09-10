package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.LmuWindowsWeatherForecast

interface LmuWindowsWeatherForecastRepository {
    suspend fun weatherForecasts(): List<LmuWindowsWeatherForecast>
}
