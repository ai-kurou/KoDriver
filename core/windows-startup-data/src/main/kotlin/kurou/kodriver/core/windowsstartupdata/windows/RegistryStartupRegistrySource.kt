package kurou.kodriver.core.windowsstartupdata.windows

private const val STARTUP_REGISTRY_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Run"
private const val STARTUP_REGISTRY_VALUE_NAME = "KoDriver"

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
