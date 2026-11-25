package kurou.kodriver.core.windowsstartupdata.windows

/** 現在のユーザーのレジストリを操作する外部APIの境界。 */
internal interface StartupRegistryApi {
    fun valueExists(
        key: String,
        valueName: String,
    ): Boolean

    /** バイナリ値を返す。値がない場合は `null`。 */
    fun getBinaryValue(
        key: String,
        valueName: String,
    ): ByteArray?

    fun setStringValue(
        key: String,
        valueName: String,
        value: String,
    )

    fun deleteValue(
        key: String,
        valueName: String,
    )
}
