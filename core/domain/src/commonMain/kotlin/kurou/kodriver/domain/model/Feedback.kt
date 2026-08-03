package kurou.kodriver.domain.model

data class Feedback(
    val type: FeedbackType,
    val message: String,
    val email: String? = null,
    val name: String? = null,
    val includesDiagnostics: Boolean = false,
)
