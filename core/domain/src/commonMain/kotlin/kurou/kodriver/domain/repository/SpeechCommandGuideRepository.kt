package kurou.kodriver.domain.repository

import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechCommandGuideGroup

/**
 * 音声タブの一覧に出す「読み上げ項目と、それを読み上げさせる語句」を取得する Repository。
 */
interface SpeechCommandGuideRepository {
    /**
     * [simulator] の案内をカテゴリごとにまとめて返す。
     * カテゴリは [kurou.kodriver.domain.model.ManualReadoutItemCategory] の順、項目は定義順で、語句を持たない項目とカテゴリは含めない。
     * 未登録・手動読み上げ非対応のシミュレーターは空を返す。
     */
    fun guideGroups(simulator: Simulator): List<SpeechCommandGuideGroup>
}
