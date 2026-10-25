package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ManualReadoutSetPreferences(
    @ProtoNumber(1) val sets: List<ManualReadoutSetPreference> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ManualReadoutSetPreference(
    @ProtoNumber(1) val id: String = "",
    @ProtoNumber(2) val itemKeys: List<String> = emptyList(),
    @ProtoNumber(3) val itemTexts: Map<String, String> = emptyMap(),
    @ProtoNumber(4) val hotkeyVirtualKeyCode: Int? = null,
    @ProtoNumber(5) val hotkeyModifiers: Int? = null,
    @ProtoNumber(6) val name: String = "",
    @ProtoNumber(7) val number: Int = 0,
)
