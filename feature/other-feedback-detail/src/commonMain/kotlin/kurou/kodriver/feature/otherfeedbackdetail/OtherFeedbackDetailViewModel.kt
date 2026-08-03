package kurou.kodriver.feature.otherfeedbackdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.model.Feedback
import kurou.kodriver.domain.model.FeedbackType
import kurou.kodriver.domain.usecase.SendFeedbackUseCase

internal class OtherFeedbackDetailViewModel(
    private val sendFeedback: SendFeedbackUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OtherFeedbackDetailUiState())
    val uiState: StateFlow<OtherFeedbackDetailUiState> = _uiState

    fun onTypeSelected(type: FeedbackType) {
        _uiState.update { it.copy(type = type, isSent = false, sendFailed = false) }
    }

    fun onMessageChanged(message: String) {
        _uiState.update {
            it.copy(
                message = message,
                isSent = false,
                sendFailed = false,
                showMessageError = false,
            )
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, isSent = false, sendFailed = false) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, isSent = false, sendFailed = false) }
    }

    fun onIncludesDiagnosticsChanged(includesDiagnostics: Boolean) {
        _uiState.update { it.copy(includesDiagnostics = includesDiagnostics, isSent = false, sendFailed = false) }
    }

    fun onSend() {
        val current = _uiState.value
        if (current.message.isBlank()) {
            _uiState.update { it.copy(showMessageError = true, isSent = false, sendFailed = false) }
            return
        }
        if (current.isSending) return
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
                            includesDiagnostics = current.includesDiagnostics,
                        ),
                    )
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
