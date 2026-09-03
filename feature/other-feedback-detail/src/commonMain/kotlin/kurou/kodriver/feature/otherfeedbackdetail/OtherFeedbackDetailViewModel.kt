package kurou.kodriver.feature.otherfeedbackdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase
import kurou.kodriver.domain.usecase.SendFeedbackUseCase

internal class OtherFeedbackDetailViewModel(
    private val sendFeedback: SendFeedbackUseCase,
    private val observeTelemetryLogDetail: ObserveTelemetryLogDetailUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OtherFeedbackDetailUiState())
    private val _telemetryLogId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OtherFeedbackDetailUiState> =
        combine(
            _uiState,
            _telemetryLogId.flatMapLatest { id ->
                if (id == null) {
                    flowOf(null)
                } else {
                    observeTelemetryLogDetail(id).map { it?.current }
                }
            },
        ) { state, attachedTelemetryLog ->
            state.copy(attachedTelemetryLog = attachedTelemetryLog)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OtherFeedbackDetailUiState(),
        )

    fun setTelemetryLogId(id: Long?) {
        _telemetryLogId.update { id }
    }

    fun onDetachTelemetryLog() {
        _telemetryLogId.update { null }
    }

    fun onTypeSelected(type: FeedbackType) {
        _uiState.update { it.copy(type = type, sendStatus = it.sendStatus.resetIfNotSending()) }
    }

    fun onMessageChanged(message: String) {
        _uiState.update {
            it.copy(
                message = message.take(FEEDBACK_MESSAGE_MAX_LENGTH),
                sendStatus = it.sendStatus.resetIfNotSending(),
                showMessageError = false,
            )
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                name = name.take(FEEDBACK_NAME_MAX_LENGTH),
                sendStatus = it.sendStatus.resetIfNotSending(),
                showNameError = false,
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email.take(FEEDBACK_EMAIL_MAX_LENGTH),
                sendStatus = it.sendStatus.resetIfNotSending(),
                showEmailError = false,
            )
        }
    }

    private fun FeedbackSendStatus.resetIfNotSending(): FeedbackSendStatus =
        if (this == FeedbackSendStatus.Sending) this else FeedbackSendStatus.Idle

    fun onSend() {
        val current = _uiState.value
        val hasInputError =
            current.message.isBlank() || current.name.isBlank() || !isValidEmail(current.email)
        if (hasInputError) {
            _uiState.update {
                it.copy(
                    showMessageError = current.message.isBlank(),
                    showNameError = current.name.isBlank(),
                    showEmailError = !isValidEmail(current.email),
                    sendStatus = current.sendStatus.resetIfNotSending(),
                )
            }
            return
        }
        if (current.sendStatus == FeedbackSendStatus.Sending) return
        val attachedTelemetryLog = uiState.value.attachedTelemetryLog
        _uiState.update { it.copy(sendStatus = FeedbackSendStatus.Sending) }
        viewModelScope.launch {
            try {
                val result =
                    sendFeedback(
                        Feedback(
                            type = current.type,
                            message = current.message,
                            email = current.email,
                            name = current.name,
                            includesDiagnostics = true,
                            telemetryLogId = attachedTelemetryLog?.id,
                            telemetryLogJson = attachedTelemetryLog?.telemetryJson,
                        ),
                    )
                if (result.isSuccess) {
                    _telemetryLogId.update { null }
                }
                _uiState.update {
                    if (result.isSuccess) {
                        OtherFeedbackDetailUiState(type = it.type, sendStatus = FeedbackSendStatus.Sent)
                    } else {
                        it.copy(sendStatus = FeedbackSendStatus.Failed)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                captureOtherFeedbackDetailError(e)
                _uiState.update { it.copy(sendStatus = FeedbackSendStatus.Failed) }
            }
        }
    }
}
