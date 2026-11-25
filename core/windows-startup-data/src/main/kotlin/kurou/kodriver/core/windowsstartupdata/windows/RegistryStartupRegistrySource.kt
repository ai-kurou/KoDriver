package kurou.kodriver.core.windowsstartupdata.windows

import kurou.kodriver.domain.captureError

private const val STARTUP_REGISTRY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"
private const val STARTUP_REGISTRY_VALUE_NAME = "KoDriver"

// タスクマネージャー等での有効・無効が保存されるキー。データ形式は公式に公開されていないため、
// 実機で確認できた「先頭バイトが3なら無効」だけを判定に使う。
private const val STARTUP_APPROVED_REGISTRY_KEY =
    "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\StartupApproved\\Run"
private const val STARTUP_APPROVED_DISABLED: Byte = 3

/** 外部APIを差し替え可能にし、自動起動の登録・解除を行う。 */
internal class RegistryStartupRegistrySource(
    private val registry: StartupRegistryApi = JnaStartupRegistryApi(),
    private val commandProvider: () -> String? = {
        ProcessHandle
            .current()
            .info()
            .command()
            .orElse(null)
    },
) : WindowsStartupRegistrySource {
    override fun isRegistered(): Boolean = registry.valueExists(STARTUP_REGISTRY_KEY, STARTUP_REGISTRY_VALUE_NAME)

    @Suppress("TooGenericExceptionCaught")
    override fun isDisabledByWindows(): Boolean =
        try {
            val value = registry.getBinaryValue(STARTUP_APPROVED_REGISTRY_KEY, STARTUP_REGISTRY_VALUE_NAME)
            value?.firstOrNull() == STARTUP_APPROVED_DISABLED
        } catch (e: RuntimeException) {
            // 読み取れない場合は、誤った警告を避けるため無効と扱わない。
            captureError(e)
            false
        }

    override fun register() {
        val command =
            commandProvider()
                ?: throw IllegalStateException("実行中のアプリケーションの起動コマンドを取得できません")
        registry.setStringValue(STARTUP_REGISTRY_KEY, STARTUP_REGISTRY_VALUE_NAME, command)
    }

    override fun unregister() {
        if (isRegistered()) {
            registry.deleteValue(STARTUP_REGISTRY_KEY, STARTUP_REGISTRY_VALUE_NAME)
        }
    }
}
