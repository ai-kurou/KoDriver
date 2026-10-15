package kurou.kodriver.domain.model

/** Windows の MOD_ALT=1、MOD_CONTROL=2、MOD_SHIFT=4 に対応する修飾キー。 */
enum class ManualReadoutHotkeyModifiers(
    val value: Int,
) {
    CTRL_ALT_SHIFT(1 or 2 or 4),
    CTRL_ALT(1 or 2),
    CTRL_SHIFT(2 or 4),
    ALT_SHIFT(1 or 4),
    ;

    companion object {
        fun fromValue(value: Int?): ManualReadoutHotkeyModifiers =
            entries.find { it.value == value } ?: MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
    }
}
