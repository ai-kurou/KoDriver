package kurou.kodriver.domain.repository

/**
 * OS起動時にKoDriverを自動起動する設定を取得・設定するRepository。
 * Windowsではレジストリ（`HKCU\Software\Microsoft\Windows\CurrentVersion\Run`）への登録・解除で実現する。
 */
public interface StartupEnabledRepository {
    public suspend fun isEnabled(): Boolean

    public suspend fun setEnabled(enabled: Boolean)

    /** OS側の設定で自動起動が無効にされているか。判定できない場合は `false` を返す。 */
    public suspend fun isDisabledByOs(): Boolean
}
