package kurou.kodriver.feature.speechrecognitionlist

import kurou.kodriver.domain.speechrecognition.model.SpeechCommandGuideGroup

/**
 * 音声タブの一覧の表示状態。選択中のシミュレーターの案内を、カテゴリごとにまとめて持つ。
 * [isAceSelected] は ACE を選択中のとき、正式リリース前の案内を出すための値。[highlight] は、直近に読み上げられた依頼の語句を強調するための指示。強調しない間は null。
 */
internal data class SpeechRecognitionListUiState(
    val groups: List<SpeechCommandGuideGroup> = emptyList(),
    val highlight: SpeechPhraseHighlight? = null,
    val isAceSelected: Boolean = false,
)

/**
 * 読み上げに至った依頼の、項目 [itemKey] と決め手になった語句 [phrase] を強調する指示。
 * 同じ語句を続けて言っても強調をやり直せるよう、読み上げのたびに [sequence] を増やす。
 */
internal data class SpeechPhraseHighlight(
    val itemKey: String,
    val phrase: String,
    val sequence: Int,
)
