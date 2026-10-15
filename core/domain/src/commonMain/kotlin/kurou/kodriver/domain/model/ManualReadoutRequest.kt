package kurou.kodriver.domain.model

/** シミュレーターの指定セットを読み上げる依頼。 */
data class ManualReadoutRequest(
    val simulatorId: String,
    val setId: String,
)
