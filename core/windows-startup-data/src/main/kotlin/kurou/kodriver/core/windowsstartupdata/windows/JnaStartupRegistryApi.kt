package kurou.kodriver.core.windowsstartupdata.windows

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg

/** JNAを直接呼ぶWindows専用の薄い実装。ユニットテストとカバレッジの対象外とする。 */
internal class JnaStartupRegistryApi : StartupRegistryApi {
    override fun valueExists(
        key: String,
        valueName: String,
    ): Boolean = Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, key, valueName)

    override fun getBinaryValue(
        key: String,
        valueName: String,
    ): ByteArray? =
        if (valueExists(key, valueName)) {
            Advapi32Util.registryGetBinaryValue(WinReg.HKEY_CURRENT_USER, key, valueName)
        } else {
            null
        }

    override fun setStringValue(
        key: String,
        valueName: String,
        value: String,
    ) {
        Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, key, valueName, value)
    }

    override fun deleteValue(
        key: String,
        valueName: String,
    ) {
        Advapi32Util.registryDeleteValue(WinReg.HKEY_CURRENT_USER, key, valueName)
    }
}
