package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.THEME_MODE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ThemePreferences(
    @ProtoNumber(1) val mode: String = THEME_MODE_DEFAULT.id,
)
