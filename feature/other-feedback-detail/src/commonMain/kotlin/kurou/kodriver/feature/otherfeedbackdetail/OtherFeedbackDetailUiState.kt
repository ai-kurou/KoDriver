package kurou.kodriver.feature.otherfeedbackdetail

import kurou.kodriver.domain.model.FeedbackType

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
) {
    val canSend: Boolean
        get() = message.isNotBlank() && name.isNotBlank() && email.isNotBlank() && !isSending
}
