package kurou.kodriver.data.preferences

import kurou.kodriver.domain.repository.SelectedAudioInputDevicePreferencesRepository

/**
 * 選択した音声入力デバイス名の Repository の永続化実装を生成する。
 */
fun createSelectedAudioInputDevicePreferencesRepository(
    directory: String,
): SelectedAudioInputDevicePreferencesRepository =
    SelectedAudioInputDevicePreferencesRepositoryImpl(createSelectedAudioInputDevicePreferencesDataStore(directory))
