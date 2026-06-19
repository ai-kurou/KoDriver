package kurou.kodriver.domain.model

enum class ReadoutStartSoundType(
    val id: String,
) {
    FORMULA_RADIO("formula_radio"),
    ELECTRONIC_NOISE("electronic_noise"),
    ;

    companion object {
        fun fromId(id: String): ReadoutStartSoundType =
            entries.firstOrNull { it.id == id } ?: FORMULA_RADIO
    }
}
