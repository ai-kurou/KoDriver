package kurou.kodriver.domain.model

enum class ReadoutStartSoundType(val id: String) {
    ELECTRONIC_NOISE("electronic_noise"),
    FORMULA_RADIO("formula_radio"),
    ;
    companion object {
        fun fromId(id: String): ReadoutStartSoundType =
            entries.firstOrNull { it.id == id } ?: ELECTRONIC_NOISE
    }
}
