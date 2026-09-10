package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.LmuWindowsWeatherForecast
import kurou.kodriver.domain.repository.LmuWindowsWeatherForecastRepository

class GetLmuWindowsWeatherForecastUseCase(
    private val repository: LmuWindowsWeatherForecastRepository,
) {
    suspend operator fun invoke(): List<LmuWindowsWeatherForecast> = repository.weatherForecasts()
}
