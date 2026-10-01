package kurou.kodriver.domain.model

/** LMU のフラッグ読み上げ項目のうち、カスタム文言と収録音声の切り替え設定を持つもの。 */
enum class LmuWindowsFlagReadoutTarget {
    SECTOR_YELLOW_FLAG,
    BLUE_FLAG,
    FULL_COURSE_YELLOW,
    RED_FLAG,
}
