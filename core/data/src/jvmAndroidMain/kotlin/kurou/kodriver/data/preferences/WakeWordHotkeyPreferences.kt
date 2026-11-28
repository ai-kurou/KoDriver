package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class WakeWordHotkeyPreferences(
    @ProtoNumber(1) val virtualKeyCode: Int? = null,
    @ProtoNumber(2) val modifiers: Int? = null,
)
