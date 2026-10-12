package kurou.kodriver.data.preferences

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT

@OptIn(ExperimentalSerializationApi::class)
@Serializable
internal data class ReadoutHotkeyPreferences(
    @ProtoNumber(1) val virtualKeyCode: Int = LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT,
)
