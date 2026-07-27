package kurou.kodriver.domain.model

sealed class Simulator(
    val id: String,
    val requiresKoDriverServer: Boolean,
) {
    data object LmuWindows : Simulator(id = "lmu_windows", requiresKoDriverServer = true)
    data object Gt7Ps5 : Simulator(id = "gt7_ps5", requiresKoDriverServer = false)
    data object AceWindows : Simulator(id = "ace_windows", requiresKoDriverServer = true)

    companion object {
        private val entries by lazy { listOf(LmuWindows, Gt7Ps5, AceWindows) }

        // nullは「まだシミュレーターが選択されていない」という正当な初期状態を表すため、非null化してデフォルト値にフォールバックしてはならない。
        fun fromId(id: String): Simulator? = entries.find { it.id == id }
    }
}
