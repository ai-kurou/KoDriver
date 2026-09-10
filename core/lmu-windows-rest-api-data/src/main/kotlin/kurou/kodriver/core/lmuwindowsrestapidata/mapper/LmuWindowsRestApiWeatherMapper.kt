package kurou.kodriver.core.lmuwindowsrestapidata.mapper

import kurou.kodriver.core.lmuwindowsrestapidata.dto.SessionWeatherDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherFieldDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherForecastResponseDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherNodeDto
import kurou.kodriver.domain.model.LmuWindowsWeatherForecast
import kurou.kodriver.domain.model.LmuWindowsWeatherForecastNode
import kurou.kodriver.domain.model.LmuWindowsWeatherForecastNodeData
import kurou.kodriver.domain.model.LmuWindowsWeatherSessionType

/**
 * `/rest/sessions/weather` のDTOをドメインモデルへ変換する。存在しないセッション種別・ノードはスキップする。
 * `WNV_WINDSPEED` の `currentValue` は km/h とは異なる内部単位のため、`× 3.6` してkm/hへ変換する
 * （docs/lmu-windows-rest-api.md 参照。`stringValue` はロケール依存の文字化けリスクがあるため使用しない）。
 */
internal object LmuWindowsRestApiWeatherMapper {
    private const val WIND_SPEED_INTERNAL_TO_KPH = 3.6

    fun map(dto: WeatherForecastResponseDto): List<LmuWindowsWeatherForecast> =
        listOfNotNull(
            mapSession(LmuWindowsWeatherSessionType.PRACTICE, dto.practice),
            mapSession(LmuWindowsWeatherSessionType.QUALIFY, dto.qualify),
            mapSession(LmuWindowsWeatherSessionType.RACE, dto.race),
        )

    private fun mapSession(
        sessionType: LmuWindowsWeatherSessionType,
        session: SessionWeatherDto?,
    ): LmuWindowsWeatherForecast? {
        if (session == null) return null

        val nodes =
            listOfNotNull(
                mapNode(LmuWindowsWeatherForecastNode.START, session.start),
                mapNode(LmuWindowsWeatherForecastNode.NODE_25, session.node25),
                mapNode(LmuWindowsWeatherForecastNode.NODE_50, session.node50),
                mapNode(LmuWindowsWeatherForecastNode.NODE_75, session.node75),
                mapNode(LmuWindowsWeatherForecastNode.FINISH, session.finish),
            )
        if (nodes.isEmpty()) return null

        return LmuWindowsWeatherForecast(sessionType = sessionType, nodes = nodes)
    }

    private fun mapNode(
        node: LmuWindowsWeatherForecastNode,
        dto: WeatherNodeDto?,
    ): LmuWindowsWeatherForecastNodeData? {
        if (dto == null) return null

        return LmuWindowsWeatherForecastNodeData(
            node = node,
            skyIndex = dto.sky.currentValueOrZero().toInt(),
            temperatureCelsius = dto.temperature.currentValueOrZero().toInt(),
            rainChancePercent = dto.rainChance.currentValueOrZero().toInt(),
            humidityPercent = dto.humidity.currentValueOrZero().toInt(),
            windDirectionIndex = dto.windDirection.currentValueOrZero().toInt(),
            windSpeedKph = dto.windSpeed.currentValueOrZero() * WIND_SPEED_INTERNAL_TO_KPH,
        )
    }

    private fun WeatherFieldDto?.currentValueOrZero(): Double = this?.currentValue ?: 0.0
}
