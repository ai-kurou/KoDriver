package kurou.kodriver.core.speechrecognitiondata.speech

import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SpeechCommandGuideGroup
import kurou.kodriver.domain.model.toSpeechCommandGuideGroups
import kurou.kodriver.domain.repository.SpeechCommandGuideRepository

/**
 * [ReadoutItemKeyRegistry] に登録された手動読み上げの項目定義と語句から、音声タブの案内を組み立てる。
 * 語句は音声認識の照合（`match*ManualReadoutPhrase`）と同じ定義なので、一覧に出る言い方と実際に反応する言い方は一致する。
 */
class RegistrySpeechCommandGuideRepository : SpeechCommandGuideRepository {
    override fun guideGroups(simulator: Simulator): List<SpeechCommandGuideGroup> =
        ReadoutItemKeyRegistry.manualReadoutSpecOf(simulator.id)?.toSpeechCommandGuideGroups().orEmpty()
}
