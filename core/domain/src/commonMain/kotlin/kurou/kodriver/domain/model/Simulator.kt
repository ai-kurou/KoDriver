package kurou.kodriver.domain.model

sealed class Simulator(
    val id: String,
    val requiresKoDriverServer: Boolean,
) {
    data object LmuWindows : Simulator(id = "lmu_windows", requiresKoDriverServer = true)
    data object Gt7Ps5 : Simulator(id = "gt7_ps5", requiresKoDriverServer = false)

    companion object {
        private val entries by lazy { listOf(LmuWindows, Gt7Ps5) }
        fun fromId(id: String): Simulator? = entries.find { it.id == id }
    }
}
