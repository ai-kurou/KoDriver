package kurou.kodriver.domain.manualreadout.model

public sealed interface HotkeyRegistrationState {
    public data object Unsupported : HotkeyRegistrationState

    public data object Registering : HotkeyRegistrationState

    public data object Registered : HotkeyRegistrationState

    public data object Conflict : HotkeyRegistrationState

    public data class Failed(
        val errorCode: Int,
    ) : HotkeyRegistrationState
}
