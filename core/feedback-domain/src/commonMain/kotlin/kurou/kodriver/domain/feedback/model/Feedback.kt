package kurou.kodriver.domain.feedback.model

public data class Feedback(
    val type: FeedbackType,
    val message: String,
    val email: String? = null,
    val name: String? = null,
    val includesDiagnostics: Boolean = false,
    val telemetryLogId: Long? = null,
    val telemetryLogJson: String? = null,
)
