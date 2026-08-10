package kurou.kodriver.domain.usecase

import kurou.kodriver.domain.model.ReadoutItemKey
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * [ReadoutItemKey] の全定義が、対応するシミュレータの `Determine*NarratorReadoutUseCase` の
 * 判定ロジックから実際に参照されていることを検証する。
 *
 * 過去に発生した配線漏れ（#464, #472）は目視確認に頼っており機械的な検出手段がなかった。
 * このテストは各キーの完全修飾名（例: `ReadoutItemKey.LmuWindows.Flag.BlueFlag`）が
 * 対応する UseCase のソースコード中に出現するかを検証することで、新しいキーを追加した際に
 * Narrator 側への配線を忘れた場合に機械的に失敗させる。
 */
class ReadoutItemKeyNarratorWiringTest {
    @Test
    fun `全てのReadoutItemKeyが対応するDetermine系UseCaseのソースから参照されている`() {
        ReadoutItemKey.entries.forEach { key ->
            val identifier = key.qualifiedIdentifier()
            val sourceFile = key.determineUseCaseSourceFile()
            val sourceText = sourceFile.readText()

            assertTrue(
                sourceText.contains(identifier),
                "$identifier が ${sourceFile.path} から参照されていません。" +
                    "Determine*NarratorReadoutUseCase の判定ロジックへの配線を確認してください。",
            )
        }
    }

    private fun ReadoutItemKey.qualifiedIdentifier(): String =
        this::class
            .qualifiedName
            ?.substringAfter("$MODEL_PACKAGE.")
            ?: error("qualifiedName を解決できませんでした: $this")

    private fun ReadoutItemKey.determineUseCaseSourceFile(): File =
        when (this) {
            is ReadoutItemKey.LmuWindows -> sourceFile(LMU_WINDOWS_USE_CASE_RELATIVE_PATH)
            is ReadoutItemKey.Gt7Ps5 -> sourceFile(GT7_PS5_USE_CASE_RELATIVE_PATH)
            is ReadoutItemKey.AceWindows -> sourceFile(ACE_WINDOWS_USE_CASE_RELATIVE_PATH)
        }

    private fun sourceFile(relativePath: String): File {
        val candidates =
            listOf(
                File(relativePath),
                File(MODULE_DIRECTORY_FROM_REPO_ROOT, relativePath),
            )
        return candidates.firstOrNull { it.exists() }
            ?: error("ソースファイルが見つかりません: $relativePath（候補: $candidates）")
    }

    private companion object {
        const val MODEL_PACKAGE = "kurou.kodriver.domain.model"
        const val MODULE_DIRECTORY_FROM_REPO_ROOT = "core/domain"
        const val LMU_WINDOWS_USE_CASE_RELATIVE_PATH =
            "src/commonMain/kotlin/kurou/kodriver/domain/usecase/DetermineLmuWindowsNarratorReadoutUseCase.kt"
        const val GT7_PS5_USE_CASE_RELATIVE_PATH =
            "src/commonMain/kotlin/kurou/kodriver/domain/usecase/DetermineGt7Ps5NarratorReadoutUseCase.kt"
        const val ACE_WINDOWS_USE_CASE_RELATIVE_PATH =
            "src/commonMain/kotlin/kurou/kodriver/domain/usecase/DetermineAceWindowsNarratorReadoutUseCase.kt"
    }
}
