package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.model.TelemetryLog

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal const val FEEDBACK_MESSAGE_MAX_LENGTH = 2000
internal const val FEEDBACK_NAME_MAX_LENGTH = 50
internal const val FEEDBACK_EMAIL_MAX_LENGTH = 254

internal fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

sealed interface FeedbackSendStatus {
    data object Idle : FeedbackSendStatus

    data object Sending : FeedbackSendStatus

    data object Sent : FeedbackSendStatus

    data object Failed : FeedbackSendStatus
}

data class OtherFeedbackDetailUiState(
    val type: FeedbackType = FeedbackType.BugReport,
    val message: String = "",
    val name: String = "",
    val email: String = "",
    val sendStatus: FeedbackSendStatus = FeedbackSendStatus.Idle,
    val showMessageError: Boolean = false,
    val showNameError: Boolean = false,
    val showEmailError: Boolean = false,
    val attachedTelemetryLog: TelemetryLog? = null,
) {
    val canSend: Boolean
        get() =
            message.isNotBlank() && name.isNotBlank() && isValidEmail(email) &&
                sendStatus != FeedbackSendStatus.Sending

    val showEmailFormatError: Boolean
        get() = showEmailError && email.isNotBlank()
}
