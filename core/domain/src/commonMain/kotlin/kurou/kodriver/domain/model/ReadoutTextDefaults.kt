package kurou.kodriver.domain.model

/** カスタム読み上げ文言として保存できる最大文字数。 */
const val READOUT_CUSTOM_TEXT_MAX_LENGTH = 40

/**
 * カスタム読み上げ文言を保存用に正規化する。
 *
 * 前後の空白を除去し、空白のみの入力は未設定（空文字）にする。
 * UI 側でも入力を制限するが、保存値の上限は仕様値として [READOUT_CUSTOM_TEXT_MAX_LENGTH] で切り詰める。
 */
fun String.normalizeReadoutCustomText(): String = trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
