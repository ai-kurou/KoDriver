package kurou.kodriver.core.lmuwindowsrestapidata.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * `GET /rest/sessions/weather` のレスポンス構造（docs/lmu-windows-rest-api.md 参照）。
 * セッション種別ごとに5つの予報ノードを持ち、各ノードが6つの気象フィールドを持つ。
 */
@Serializable
internal data class WeatherForecastResponseDto(
    @SerialName("PRACTICE") val practice: SessionWeatherDto? = null,
    @SerialName("QUALIFY") val qualify: SessionWeatherDto? = null,
    @SerialName("RACE") val race: SessionWeatherDto? = null,
)

@Serializable
internal data class SessionWeatherDto(
    @SerialName("START") val start: WeatherNodeDto? = null,
    @SerialName("NODE_25") val node25: WeatherNodeDto? = null,
    @SerialName("NODE_50") val node50: WeatherNodeDto? = null,
    @SerialName("NODE_75") val node75: WeatherNodeDto? = null,
    @SerialName("FINISH") val finish: WeatherNodeDto? = null,
)

@Serializable
internal data class WeatherNodeDto(
    @SerialName("WNV_SKY") val sky: WeatherFieldDto? = null,
    @SerialName("WNV_TEMPERATURE") val temperature: WeatherFieldDto? = null,
    @SerialName("WNV_RAIN_CHANCE") val rainChance: WeatherFieldDto? = null,
    @SerialName("WNV_HUMIDITY") val humidity: WeatherFieldDto? = null,
    @SerialName("WNV_WINDDIRECTION") val windDirection: WeatherFieldDto? = null,
    @SerialName("WNV_WINDSPEED") val windSpeed: WeatherFieldDto? = null,
)

@Serializable
internal data class WeatherFieldDto(
    val currentValue: Double = 0.0,
    val stringValue: String = "",
)
