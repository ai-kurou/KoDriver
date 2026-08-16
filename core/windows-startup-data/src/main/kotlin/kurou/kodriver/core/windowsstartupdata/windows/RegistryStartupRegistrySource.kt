package kurou.kodriver.core.windowsstartupdata.windows

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg

private const val STARTUP_REGISTRY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"
private const val STARTUP_REGISTRY_VALUE_NAME = "KoDriver"

/**
 * `Advapi32Util`（JNA）を介してレジストリの`Run`キーへ現在実行中のアプリの起動コマンドを登録・解除する。
 * プラットフォーム固有の外部APIを直接呼び出すためユニットテスト対象外とし（CLAUDE.mdのテスト方針を参照）、
 * 上位の [kurou.kodriver.core.windowsstartupdata.repository.WindowsStartupEnabledRepository]
 * 側のロジックを [WindowsStartupRegistrySource] 経由のFakeでテストする。
 */
internal class RegistryStartupRegistrySource : WindowsStartupRegistrySource {
    override fun isRegistered(): Boolean =
        Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, STARTUP_REGISTRY_KEY, STARTUP_REGISTRY_VALUE_NAME)

    override fun register() {
        val command =
            ProcessHandle.current().info().command().orElseThrow {
                IllegalStateException("実行中のアプリケーションの起動コマンドを取得できません")
            }
        Advapi32Util.registrySetStringValue(
            WinReg.HKEY_CURRENT_USER,
            STARTUP_REGISTRY_KEY,
            STARTUP_REGISTRY_VALUE_NAME,
            command,
        )
    }

    override fun unregister() {
        if (isRegistered()) {
            Advapi32Util.registryDeleteValue(
                WinReg.HKEY_CURRENT_USER,
                STARTUP_REGISTRY_KEY,
                STARTUP_REGISTRY_VALUE_NAME,
            )
        }
    }
}
