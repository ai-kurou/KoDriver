package kurou.kodriver.feature.otherwakewordhotkeydetail

/** 割り当てようとした組み合わせを使っている手動読み上げセット。 */
internal data class OtherWakeWordHotkeyConflict(
    val simulatorId: String,
    val setNumber: Int,
    val name: String = "",
)
