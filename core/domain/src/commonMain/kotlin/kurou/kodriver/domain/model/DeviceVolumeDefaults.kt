package kurou.kodriver.domain.model

const val DEVICE_VOLUME_MIN = 0
const val DEVICE_VOLUME_MAX = 100

/** その他一覧画面のデバイス音量を取得する間隔（ミリ秒）。画面ごとの更新間隔であり、他画面の間隔とは独立して扱う。 */
const val OTHER_LIST_DEVICE_VOLUME_POLLING_INTERVAL_MS = 2_000L

/** その他の音量詳細画面のデバイス音量を取得する間隔（ミリ秒）。画面ごとの更新間隔であり、他画面の間隔とは独立して扱う。 */
const val OTHER_VOLUME_DETAIL_DEVICE_VOLUME_POLLING_INTERVAL_MS = 500L
