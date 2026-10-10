# text-to-speech-data

OS標準の音声合成（TTS）で任意のテキストを読み上げるRepositoryを実装するJVM / Androidマルチプラットフォームモジュールです。
開始音のWAVを再生する`:core:narrator`とは別に、ラップタイムのような動的な自由文言の本文を
読み上げる手段を提供します。

- jvmMain: PowerShellの`System.Speech.Synthesis.SpeechSynthesizer`経由でWindows標準の音声合成（SAPI）を呼び出す
  `WindowsTextToSpeechRepository`。JVMには音声合成の標準APIが無く、SAPIのCOMインターフェースをJNAで直接扱うには
  型ライブラリのバインディングが必要になるため、PowerShellプロセスを1つ常駐させて`SpeechSynthesizer`を使い回す
  （`SapiSpeechSynthesizer`、`ResidentSpeechSession`）。読み上げごとにプロセスを起動するとPowerShellと.NETの起動・
  `System.Speech`のロードが毎回かかって発話が遅れるため。標準入力へ`SPEAK <音量> <Rate> <Pitch半音> <音声IDのBase64> <テキストのBase64>`の
  要求行を送り、完了（打ち切りを含む）は標準出力の`DONE`行で受け取る。読み上げの中断は`STOP`行で行い、
  常駐プロセスが終了していれば次の読み上げで起動し直す。利用可否の判定でWindows音声が使えると分かった時点で
  `warmUp()`により常駐プロセスを先に起動する。非Windowsでは`isAvailable()`が`false`を返し、読み上げも行わない。
  Windowsでは`isAvailable()`が、有効な日本語（`ja-JP`）音声がSAPIに1つ以上あるかをPowerShellで判定する（外部プロセスの
  起動を伴うため、利用できると判明した後は結果を保持する）。利用できない場合、`unavailableReason()`は
  `WindowsSpeechUnavailable`を返し、その他タブにWindowsの音声設定への案内が表示される。非Windowsでは`null`を返す。
- androidMain: `android.speech.tts.TextToSpeech`を使う`AndroidTextToSpeechRepository`。`TextToSpeech`の初期化は
  非同期（`OnInitListener`）で完了するため、最初の読み上げ要求時に初期化の完了を待ち合わせる。初期化失敗時・
  読み上げ言語（既定は日本語）が利用できない場合はエンジンを破棄し、以降の読み上げを行わない。
  利用できない理由は`unavailableReason()`で`EngineMissing`（エンジン未導入・初期化失敗）と`LanguageDataMissing`
  （日本語データ未導入）に区別され、その他タブの案内に使われる。ユーザーがエンジンや日本語データを導入して
  アプリへ戻った場合に案内を消せるよう、`unavailableReason()`の呼び出し時のみ再初期化する（`speak()`や
  `isAvailable()`は利用不可の結果を保持し、読み上げのたびにエンジンを生成し直さない）。

いずれも`core:domain`の`TextToSpeechRepository`を実装し、`SpeakTextUseCase` / `StopSpeakingUseCase` /
`CheckTextToSpeechAvailableUseCase`から利用されます。

Windowsの読み上げは保存済みの`voiceId`を`SelectVoice`へ渡します。未指定なら従来どおり日本語（`ja-JP`）音声を
選び、指定音声が見つからない場合も`SelectVoiceByHints`で日本語音声へフォールバックします。
Androidは`voiceId`を`Voice.name`として検索して`setVoice`で反映します。未指定・音声が見つからない場合・
`setVoice`が失敗した場合は`setLanguage`で既定の日本語音声へ戻します。同じIDは再適用せず、初回未指定時は
初期化済みの言語設定を使います。設定はエンジン全体に残るため、キュー待ちの発話にも新しい声が適用される可能性があります。
読み上げ速度は`SpeakTextUseCase`が毎回保存済み設定を取得し、`TextToSpeechRepository.speak`の`speed`へ渡します。
`speed`は1.0が標準で、両OSのRepositoryで0.5〜2.0へ制限します。音声IDを明示した試聴でも保存済み速度を使います。
Androidは発話前に`setSpeechRate`を音声と同じロック内で適用し、成功した同じ値は再適用しません。
エンジン再初期化時は適用済み速度を破棄し、失敗した値は次の発話で再試行します。速度もエンジン全体の設定です。
Windowsは`round(10 * ln(speed) / ln(3.0))`を-10〜10へ制限して要求行の`Rate`に含め、
`SpeechSynthesizer.Rate`へ設定します。整数への丸めにより0.5 / 1.0 / 2.0倍は-6 / 0 / 6になります。
声の高さも`SpeakTextUseCase`が保存済み設定を取得し、末尾の`pitch`へ渡します。標準は1.0で、両OSで0.5〜2.0へ制限します。
Androidは`setPitch`で反映し、速度と同様に成功した値を保持して、エンジン再初期化時に破棄します。
Windowsは`12 * log2(pitch)`で-12〜12半音へ変換し、ASCIIの要求行に含めます。
SSMLの`prosody pitch`で反映し、テキストはXMLエスケープします。使用中の音声がpitchに対応しない場合は変化しない場合があります。
1.0（0半音）は従来の`SpeakAsync`、それ以外は`SpeakSsmlAsync`を使います。
常駐スクリプトと要求行の生成は純粋関数`buildResidentSpeakScript` / `buildSpeakRequest`として切り出してテストします。

Windows専用の`WindowsVoiceListRepository`は、有効なSAPI音声のID（`VoiceInfo.Name`）、表示名
（`VoiceInfo.Description`）、言語を取得します。取得はIOスレッド上で排他し、一覧はキャッシュせず毎回取得します。
詳細画面の再読み込みで、音声の追加・削除や日本語の既定音声の変更を反映します。
非Windows・失敗・15秒のタイムアウト・音声未導入の場合は空の一覧を返します。
一覧取得スクリプトは純粋関数`buildListVoicesScript`で生成し、Windowsのコマンドライン引数では二重引用符が
欠落するため、文字列の引用には単一引用符のみを使い、タブ区切りは`[char]9`で指定します。
`GetAvailableVoicesUseCase`が`ja-JP`の音声だけに絞り込み、表示名の昇順に並べます。
Androidの`AndroidVoiceListRepository`も登録し、`ja`言語・オフライン可・インストール済みの音声だけを返します。
`engineOrNull`で読み上げ用の`TextToSpeech`を1インスタンス共有し、一覧は再読み込みで導入状態を反映できるよう
キャッシュしません。初期化失敗・一覧がnull・取得例外の場合は空の一覧を返します（キャンセルは再スローします）。
出力の解析は独立した純粋関数としてユニットテストで検証します。

`SapiSpeechSynthesizer`はWindows専用の外部プロセスを起動するためユニットテストの対象外とし、読み上げ制御の
ロジックは差し替え可能な`WindowsSpeechSynthesizer`を介して`WindowsTextToSpeechRepository`側で検証します。

Windowsの音声設定の起動は、jvmMainの`WindowsSpeechSettingsSenderRepository`が担当します。
`rundll32`経由で`ms-settings:speech`を開き、非Windowsでは何もしません。起動時のIOException・
SecurityExceptionはSentryへ記録します。androidMainには、Windowsの音声設定を開けないため
何もしない`AndroidSpeechSettingsSenderRepository`を登録します。どちらも`core:domain`の
`SpeechSettingsSenderRepository`を実装し、`OpenWindowsSpeechSettingsUseCase`から利用されます。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-text-to-speech-data.svg)
<!-- MODULE-GRAPH-END -->

音声一覧には`isDefault`を付け、詳細画面で既定音声の重複表示を避ける。Windowsは読み上げと同じ日本語の`SelectVoiceByHints`で選択された音声、Androidは日本語の初期化時に`setLanguage`で選ばれた音声のIDを保持し、一覧のIDと照合する。試聴・個別音声の指定で保持したIDは変えず、エンジンの再初期化時に取得し直す。既定情報を取得できない場合は音声を除外しない。
