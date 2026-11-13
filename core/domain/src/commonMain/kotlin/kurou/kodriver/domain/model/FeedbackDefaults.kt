package kurou.kodriver.domain.model

const val FEEDBACK_COOLDOWN_DURATION_MILLIS_DEFAULT: Long = 24 * 60 * 60 * 1000L

/** フィードバック本文の最大文字数。 */
const val FEEDBACK_MESSAGE_MAX_LENGTH = 2000

/** フィードバックの名前欄の最大文字数。 */
const val FEEDBACK_NAME_MAX_LENGTH = 50

/** フィードバックのメールアドレス欄の最大文字数。 */
const val FEEDBACK_EMAIL_MAX_LENGTH = 254
