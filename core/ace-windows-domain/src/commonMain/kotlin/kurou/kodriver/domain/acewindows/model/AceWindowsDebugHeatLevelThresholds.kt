package kurou.kodriver.domain.acewindows.model

/** デバッグ状態表示でブレーキ温度を低温と分類する境界値（摂氏）。この値未満が低温。 */
public const val ACE_WINDOWS_BRAKE_TEMPERATURE_COOL_CELSIUS: Double = 400.0

/** デバッグ状態表示でブレーキ温度を過熱と分類する境界値（摂氏）。この値以上が過熱。 */
public const val ACE_WINDOWS_BRAKE_TEMPERATURE_HOT_CELSIUS: Double = 1000.0
