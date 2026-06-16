package kurou.kodriver.domain.model

enum class Simulator(
    val id: String,
    val requiresKoDriverServer: Boolean,
) {
    LMU(id = "lmu_windows", requiresKoDriverServer = true),
    ;

    companion object {
        fun fromId(id: String): Simulator? = entries.find { it.id == id }
    }
}
