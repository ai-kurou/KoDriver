package kurou.kodriver.core.lmuwindowsrestapidata.repository

import kurou.kodriver.core.lmuwindowsrestapidata.datasource.LmuWindowsRestApiWeatherDataSource
import kurou.kodriver.core.lmuwindowsrestapidata.mapper.LmuWindowsRestApiWeatherMapper
import kurou.kodriver.domain.model.LmuWindowsWeatherForecast
import kurou.kodriver.domain.repository.LmuWindowsWeatherForecastRepository

internal class LmuWindowsWeatherForecastRepositoryImpl(
    private val dataSource: LmuWindowsRestApiWeatherDataSource,
) : LmuWindowsWeatherForecastRepository {
    override suspend fun weatherForecasts(): List<LmuWindowsWeatherForecast> =
        LmuWindowsRestApiWeatherMapper.map(dataSource.fetchWeatherForecast())
}
