package kurou.kodriver.domain.model

public enum class ReadoutStartSoundType(
    public val id: String,
) {
    FORMULA_RADIO("formula_radio"),
    ELECTRONIC_NOISE("electronic_noise"),
    ;

    public companion object {
        public fun fromId(id: String): ReadoutStartSoundType =
            entries.firstOrNull { it.id == id } ?: READOUT_START_SOUND_TYPE_DEFAULT
    }
}
