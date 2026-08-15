package kurou.kodriver.domain.repository

/**
 * OS起動時にKoDriverを自動起動する設定を取得・設定するRepository。
 * Windowsではレジストリ（`HKCU\Software\Microsoft\Windows\CurrentVersion\Run`）への登録・解除で実現する。
 */
interface StartupRegistrationRepository {
    suspend fun isEnabled(): Boolean

    suspend fun setEnabled(enabled: Boolean)
}
