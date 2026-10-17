package kurou.kodriver.core.windowsstartupdata.windows

internal class FakeStartupRegistryApi : StartupRegistryApi {
    val values = mutableMapOf<Pair<String, String>, String>()
    val deletedValues = mutableListOf<Pair<String, String>>()
    var deletionFailure: RuntimeException? = null
    var failure: RuntimeException? = null

    override fun valueExists(
        key: String,
        valueName: String,
    ): Boolean {
        failure?.let { throw it }
        return values.containsKey(key to valueName)
    }

    override fun setStringValue(
        key: String,
        valueName: String,
        value: String,
    ) {
        failure?.let { throw it }
        values[key to valueName] = value
    }

    override fun deleteValue(
        key: String,
        valueName: String,
    ) {
        deletionFailure?.let { throw it }
        failure?.let { throw it }
        deletedValues.add(key to valueName)
        values.remove(key to valueName)
    }
}
