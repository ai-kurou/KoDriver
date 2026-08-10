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
        _uiState.update { it.copy(type = type, isSent = false, sendFailed = false) }
    }

    fun onMessageChanged(message: String) {
        _uiState.update {
            it.copy(
                message = message.take(FEEDBACK_MESSAGE_MAX_LENGTH),
                isSent = false,
                sendFailed = false,
                showMessageError = false,
            )
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update {
            it.copy(
                name = name.take(FEEDBACK_NAME_MAX_LENGTH),
                isSent = false,
                sendFailed = false,
                showNameError = false,
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email.take(FEEDBACK_EMAIL_MAX_LENGTH),
                isSent = false,
                sendFailed = false,
                showEmailError = false,
            )
        }
    }

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
                    isSent = false,
                    sendFailed = false,
                )
            }
            return
        }
        if (current.isSending) return
        val attachedTelemetryLog = uiState.value.attachedTelemetryLog
        _uiState.update { it.copy(isSending = true, isSent = false, sendFailed = false) }
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
                        OtherFeedbackDetailUiState(type = it.type, isSent = true)
                    } else {
                        it.copy(isSending = false, sendFailed = true)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, sendFailed = true) }
            }
        }
    }
}
