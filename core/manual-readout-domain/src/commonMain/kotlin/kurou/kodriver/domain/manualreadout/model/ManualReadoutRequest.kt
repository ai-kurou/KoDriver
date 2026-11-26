package kurou.kodriver.domain.manualreadout.model

/** シミュレーターの指定セットを読み上げる依頼。 */
public data class ManualReadoutRequest(
    val simulatorId: String,
    val setId: String,
)
