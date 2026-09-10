package kurou.kodriver.domain.model

enum class LmuWindowsWeatherSessionType {
    PRACTICE,
    QUALIFY,
    RACE,
}

enum class LmuWindowsWeatherForecastNode {
    START,
    NODE_25,
    NODE_50,
    NODE_75,
    FINISH,
}

data class LmuWindowsWeatherForecast(
    val sessionType: LmuWindowsWeatherSessionType,
    val nodes: List<LmuWindowsWeatherForecastNodeData>,
)

data class LmuWindowsWeatherForecastNodeData(
    val node: LmuWindowsWeatherForecastNode,
    val skyIndex: Int,
    val temperatureCelsius: Int,
    val rainChancePercent: Int,
    val humidityPercent: Int,
    val windDirectionIndex: Int,
    val windSpeedKph: Double,
)
