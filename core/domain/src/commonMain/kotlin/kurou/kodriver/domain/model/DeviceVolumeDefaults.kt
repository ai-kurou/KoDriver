package kurou.kodriver.domain.model

public const val DEVICE_VOLUME_MIN: Int = 0
public const val DEVICE_VOLUME_MAX: Int = 100

/** その他一覧画面のデバイス音量を取得する間隔（ミリ秒）。画面ごとの更新間隔であり、他画面の間隔とは独立して扱う。 */
public const val OTHER_LIST_DEVICE_VOLUME_POLLING_INTERVAL_MS: Long = 2_000L

/** その他の音量詳細画面のデバイス音量を取得する間隔（ミリ秒）。画面ごとの更新間隔であり、他画面の間隔とは独立して扱う。 */
public const val OTHER_VOLUME_DETAIL_DEVICE_VOLUME_POLLING_INTERVAL_MS: Long = 500L
