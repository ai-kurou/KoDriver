package kurou.kodriver.core.lmuwindowsrestapidata.mapper

import kurou.kodriver.core.lmuwindowsrestapidata.dto.SessionWeatherDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherFieldDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherForecastResponseDto
import kurou.kodriver.core.lmuwindowsrestapidata.dto.WeatherNodeDto
import kurou.kodriver.domain.model.LmuWindowsWeatherForecastNode
import kurou.kodriver.domain.model.LmuWindowsWeatherSessionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LmuWindowsRestApiWeatherMapperTest {
    @Test
    fun `mapは全セッション種別を持つDTOを全て変換する`() {
        val dto =
            WeatherForecastResponseDto(
                practice = fakeSession(),
                qualify = fakeSession(),
                race = fakeSession(),
            )

        val result = LmuWindowsRestApiWeatherMapper.map(dto)

        assertEquals(
            listOf(
                LmuWindowsWeatherSessionType.PRACTICE,
                LmuWindowsWeatherSessionType.QUALIFY,
                LmuWindowsWeatherSessionType.RACE,
            ),
            result.map { it.sessionType },
        )
    }

    @Test
    fun `mapはnullのセッション種別を結果から除外する`() {
        val dto = WeatherForecastResponseDto(practice = null, qualify = null, race = fakeSession())

        val result = LmuWindowsRestApiWeatherMapper.map(dto)

        assertEquals(listOf(LmuWindowsWeatherSessionType.RACE), result.map { it.sessionType })
    }

    @Test
    fun `mapはノードが1つも無いセッションを結果から除外する`() {
        val dto = WeatherForecastResponseDto(race = SessionWeatherDto())

        val result = LmuWindowsRestApiWeatherMapper.map(dto)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `mapは存在するノードのみ変換しwindSpeedを3_6倍してkph換算する`() {
        val dto =
            WeatherForecastResponseDto(
                race =
                    SessionWeatherDto(
                        start =
                            WeatherNodeDto(
                                sky = WeatherFieldDto(currentValue = 0.0, stringValue = "晴天"),
                                temperature = WeatherFieldDto(currentValue = 23.0, stringValue = "23 °"),
                                rainChance = WeatherFieldDto(currentValue = 0.0, stringValue = "0%"),
                                humidity = WeatherFieldDto(currentValue = 75.0, stringValue = "75%"),
                                windDirection = WeatherFieldDto(currentValue = 1.0, stringValue = "North East"),
                                windSpeed = WeatherFieldDto(currentValue = 5.0, stringValue = "18.0 kph"),
                            ),
                        node25 = null,
                        node50 = null,
                        node75 = null,
                        finish = null,
                    ),
            )

        val result = LmuWindowsRestApiWeatherMapper.map(dto)

        assertEquals(1, result.size)
        val nodes = result.single().nodes
        assertEquals(1, nodes.size)
        val startNode = nodes.single()
        assertEquals(LmuWindowsWeatherForecastNode.START, startNode.node)
        assertEquals(0, startNode.skyIndex)
        assertEquals(23, startNode.temperatureCelsius)
        assertEquals(0, startNode.rainChancePercent)
        assertEquals(75, startNode.humidityPercent)
        assertEquals(1, startNode.windDirectionIndex)
        assertEquals(18.0, startNode.windSpeedKph)
    }

    @Test
    fun `mapはフィールドが欠落しているノードを0埋めで変換する`() {
        val dto = WeatherForecastResponseDto(race = SessionWeatherDto(start = WeatherNodeDto()))

        val result = LmuWindowsRestApiWeatherMapper.map(dto)

        val startNode = result.single().nodes.single()
        assertEquals(0, startNode.skyIndex)
        assertEquals(0, startNode.temperatureCelsius)
        assertEquals(0, startNode.rainChancePercent)
        assertEquals(0, startNode.humidityPercent)
        assertEquals(0, startNode.windDirectionIndex)
        assertEquals(0.0, startNode.windSpeedKph)
    }

    private fun fakeSession() =
        SessionWeatherDto(
            start = WeatherNodeDto(temperature = WeatherFieldDto(currentValue = 20.0, stringValue = "20 °")),
        )
}
