package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.telemetrylog.model.TelemetryLog

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email)

public sealed interface FeedbackSendStatus {
    public data object Idle : FeedbackSendStatus

    public data object Sending : FeedbackSendStatus

    public data object Sent : FeedbackSendStatus

    public data object Failed : FeedbackSendStatus
}

public data class OtherFeedbackDetailUiState(
    val type: FeedbackType = FeedbackType.BugReport,
    val message: String = "",
    val name: String = "",
    val email: String = "",
    val sendStatus: FeedbackSendStatus = FeedbackSendStatus.Idle,
    val showMessageError: Boolean = false,
    val showNameError: Boolean = false,
    val showEmailError: Boolean = false,
    val attachedTelemetryLog: TelemetryLog? = null,
    val isCoolingDown: Boolean = false,
) {
    val canSend: Boolean
        get() =
            message.isNotBlank() && name.isNotBlank() && isValidEmail(email) &&
                sendStatus != FeedbackSendStatus.Sending && !isCoolingDown

    val showEmailFormatError: Boolean
        get() = showEmailError && email.isNotBlank()
}
