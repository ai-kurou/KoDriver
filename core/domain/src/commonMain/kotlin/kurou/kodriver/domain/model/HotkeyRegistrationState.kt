package kurou.kodriver.domain.model

sealed interface HotkeyRegistrationState {
    data object Unsupported : HotkeyRegistrationState

    data object Registering : HotkeyRegistrationState

    data object Registered : HotkeyRegistrationState

    data object Conflict : HotkeyRegistrationState

    data class Failed(
        val errorCode: Int,
    ) : HotkeyRegistrationState
}
