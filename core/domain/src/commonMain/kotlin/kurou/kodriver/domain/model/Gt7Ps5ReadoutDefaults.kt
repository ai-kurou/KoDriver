package kurou.kodriver.domain.model

/**
 * GT7 の読み上げ有効状態。一覧画面と Narrator が共有する。
 * 新しい TopLevel は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
 */
val GT7_PS5_READOUT_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
        Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
    )

/**
 * GT7 の開始音有効状態。全ての TopLevel をデフォルトONとする。
 * 新しい TopLevel は必ずここにも列挙すること（省略＝デフォルトtrue、ではない）。
 */
val GT7_PS5_READOUT_START_SOUND_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        Gt7Ps5ReadoutItemKey.MyBestLap.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
    )

/**
 * GT7 のキュー有効状態。supportsQueue が true の TopLevel は必ずここに列挙すること。
 * 省略＝デフォルトfalse、ではない。
 */
val GT7_PS5_QUEUE_ENABLED_STATE_DEFAULT: Map<ReadoutItemKey, Boolean> =
    mapOf(
        Gt7Ps5ReadoutItemKey.MyBestLap.Root to false,
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root to true,
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root to true,
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root to true,
    )

/** 一覧画面の TopLevel 項目と Narrator が共有する読み上げ順序の初期値。 */
internal fun gt7Ps5DefaultAutoReadoutOrder(): List<ReadoutItemKey> =
    ReadoutItemKey.entries
        .filterIsInstance<Gt7Ps5ReadoutItemKey.TopLevel>()
        .sortedBy { key -> gt7Ps5OrderIndex(key) }

// 新しい TopLevel を追加した際、ここで対応を判断しないとコンパイルが通らない。
private fun gt7Ps5OrderIndex(key: Gt7Ps5ReadoutItemKey.TopLevel): Int =
    when (key) {
        Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root -> 0
        Gt7Ps5ReadoutItemKey.RemainingFuel.Root -> 1
        Gt7Ps5ReadoutItemKey.TyreTemperature.Root -> 2
        Gt7Ps5ReadoutItemKey.MyBestLap.Root -> 3
    }
