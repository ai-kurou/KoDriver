package kurou.kodriver.feature.otherlist

/** OS起動時自動起動フローの表示状態。 */
data class OtherListStartupRegistrationUiState(
    val startupEnabled: Boolean = false,
)
