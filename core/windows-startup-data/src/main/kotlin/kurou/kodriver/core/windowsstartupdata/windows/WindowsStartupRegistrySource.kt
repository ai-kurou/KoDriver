package kurou.kodriver.core.windowsstartupdata.windows

/**
 * `HKCU\Software\Microsoft\Windows\CurrentVersion\Run` へのKoDriverの登録有無を取得・設定する。
 * 実装はJNA経由でWindowsレジストリAPIを直接呼び出すため、テストでは [RegistryStartupRegistrySource]
 * の代わりにFakeへ差し替える。
 */
internal interface WindowsStartupRegistrySource {
    fun isRegistered(): Boolean

    /** Windowsの「スタートアップ アプリ」で無効にされているか。判定できない場合は `false`。 */
    fun isDisabledByWindows(): Boolean

    fun register()

    fun unregister()
}
