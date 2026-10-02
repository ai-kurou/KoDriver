package kurou.kodriver.core.texttospeechdata.windows

import kurou.kodriver.domain.model.TextToSpeechVoice

/** タブ区切りの音声一覧を解析し、先頭のBOMを取り除いたうえで、空行と列数が不足した行を除外する。 */
internal fun parseVoiceList(output: String): List<TextToSpeechVoice> =
    output
        .removePrefix(BYTE_ORDER_MARK)
        .lineSequence()
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            val columns = line.split('\t').map { it.trim() }
            if (columns.size < VOICE_COLUMN_COUNT) {
                null
            } else {
                TextToSpeechVoice(columns[0], columns[1], columns[2])
            }
        }.toList()

private const val VOICE_COLUMN_COUNT = 3

private const val BYTE_ORDER_MARK = "\uFEFF"
