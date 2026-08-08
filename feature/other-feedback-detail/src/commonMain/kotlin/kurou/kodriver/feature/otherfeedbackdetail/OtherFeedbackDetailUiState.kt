package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.TelemetryLog

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

data class OtherFeedbackDetailUiState(
    val type: FeedbackType = FeedbackType.BugReport,
    val message: String = "",
    val name: String = "",
    val email: String = "",
    val isSending: Boolean = false,
    val isSent: Boolean = false,
    val sendFailed: Boolean = false,
    val showMessageError: Boolean = false,
    val showNameError: Boolean = false,
    val showEmailError: Boolean = false,
    val attachedTelemetryLog: TelemetryLog? = null,
) {
    val canSend: Boolean
        get() = message.isNotBlank() && name.isNotBlank() && isValidEmail(email) && !isSending

    val showEmailFormatError: Boolean
        get() = showEmailError && email.isNotBlank()
}
