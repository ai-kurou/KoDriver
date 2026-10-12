package kurou.kodriver.domain.model

/**
 * LMU の Windows 共有メモリから読み取ったプレイヤー車両中心のテレメトリ。
 *
 * 各フィールドは shared memory の生値をアプリ内で扱いやすい単位へ正規化したもの。
 * 接続断時はこの型のダミー値ではなく、Repository 側の接続状態や Flow の停止で表現する。
 */
data class LmuWindowsTelemetryData(
    /** サンプル取得時刻。単位は Unix epoch milliseconds。 */
    val timestampMs: Long,
    val engine: LmuWindowsEngineData,
    val inputs: LmuWindowsInputsData,
    val tyres: LmuWindowsTyreData,
    val fuel: LmuWindowsFuelData,
    val timing: LmuWindowsTimingData,
    val vehicle: LmuWindowsVehicleData,
)
