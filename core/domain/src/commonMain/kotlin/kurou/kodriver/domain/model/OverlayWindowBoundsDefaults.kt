package kurou.kodriver.domain.model

/**
 * オーバーレイウィンドウの位置が未設定であることを表す値。
 *
 * 初回起動時はユーザーがまだウィンドウを動かしていないため、保存された座標が存在しない。
 * 座標は負値も取りうる（マルチモニタ環境では左側・上側のモニタが負の座標になる）ため、
 * 「未設定」を負値では表現できず、実際の座標として現れない [Int.MIN_VALUE] を番兵として使う。
 */
const val OVERLAY_WINDOW_POSITION_UNSPECIFIED = Int.MIN_VALUE

const val OVERLAY_WINDOW_WIDTH_DEFAULT = 480

const val OVERLAY_WINDOW_HEIGHT_DEFAULT = 120
