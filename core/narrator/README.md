# narrator

WAV 音声を読み上げる narrator feature（`feature:lmu-windows-narrator` / `feature:gt7-ps5-narrator` /
`feature:ace-windows-narrator`）が共通で利用する、WAV 音声再生の基盤モジュールです。

`SoundPlayer` はプラットフォームごとに JVM（`javax.sound.sampled`）/ Android（`SoundPool`）/ Js / WasmJs の
実装を提供します。JVM/Android 実装は Bluetooth A2DP 接続時の音切れ対策（末尾への無音追記・アンロードタイミングの調整）を
含みます。`NarratorErrorCapture` は再生失敗を Sentry（JVM/Android のみ）へ送出する expect/actual です。

`WavNarratorEngine<EVENT, START_TYPE, KEY>` は WAV 読み上げロジックの共通実装です。`:core:domain` の
`SpeechEvent` / `ReadoutStartSoundType` / `ReadoutItemKey` を型パラメータとして受け取る形にすることで、
`:core:narrator` 自体は `:core:domain` に依存しません（`moduleGraphAssert` の `maxHeight` 制約を超えないため）。
イベント→WAVファイルパスのマップ・開始音タイプ→ファイルパスのマップ・WAV を読み込む `resourceLoader`（各 narrator
feature 自身の compose resources `Res::readBytes`）・イベントからキーへの変換関数 `eventToKey` をコンストラクタで
受け取ることで、3つの narrator feature がそのまま利用できます。各 feature は `TextToSpeechEngine` を実装する薄い
アダプタ（`LmuWindowsWavNarratorEngine` など）でこのエンジンをラップします。優先度の高いイベントで割り込む際の
`stop()` → `speak()` の連続呼び出しに対しても、直前にキャンセルした再生ジョブの停止処理が完了するまで新しい再生を
始めないよう `lastCancelledPlayback` で待ち合わせます。

`platformSoundModule(qualifier)` は `SoundPlayer` のプラットフォーム実装を、呼び出し側が指定した Koin の named
修飾子付きでバインドする expect/actual です。3つの narrator feature は同一の Koin コンテナに同時にロードされるため、
`named("lmu_windows")` / `named("gt7_ps5")` / `named("ace_windows")` のように feature ごとに異なる修飾子を渡すことで、
`SoundPlayer` の登録が衝突しないようにしています。

`speakWithPriority<KEY>(...)`（`NarratorPriority.kt`）は、3つの narrator feature の `XxxNarratorEventProcessor` が
共通で利用する優先度判定ロジックです。処理は次の4経路に分岐します。

| # | 条件 | 動作 | 対応する `NarrationOutcome` |
| --- | --- | --- | --- |
| 1 | キュー再生が有効 | 優先度判定をせず `speak(queue = true)` でキューへ追加 | `QUEUED` |
| 2 | キュー再生が無効・再生中のイベントあり・優先度が負けた | `speak` も `stop` も呼ばない | `SKIPPED` |
| 3 | キュー再生が無効・再生中のイベントあり・優先度が勝った | `stop()` してから `speak(queue = false)` | `INTERRUPTED` |
| 4 | キュー再生が無効・再生中のイベントなし | `stop()` せず `speak(queue = false)` | `SPOKEN` |

`readoutOrder` に含まれないキーは最も優先度が低いものとして扱います。

**戻り値の `Boolean` だけでは4経路を区別できません**（経路1・3・4がいずれも `true` になります）。テレメトリログへ
保存する `NarrationOutcome` のように4経路を区別したい呼び出し側は、`speak` に渡された `queue` の値と `stop` が
呼ばれたかどうかを各ラムダの中で捕捉して判別します（3つの `XxxNarratorEventProcessor` の private な
`speakWithPriority` ラッパーがこれを行っています）。特に経路4は、再生中の読み上げが無いためそのまま読み上げた
だけで `stop()` は呼ばれておらず、経路3の割り込み再生とは異なります。両者を取り違えると通常の読み上げが
「割り込み再生」として記録されます（PR #1626 で修正済み）。

`WavNarratorEngine` と同様に `:core:domain` の `SpeechEvent` / `ReadoutItemKey` へ依存しないよう、イベントのキーは
呼び出し側から値として渡し、`speak` / `stop` の実行や現在再生中のキーの取得もラムダで受け取ります。この制約の
ため、この関数自体が `:core:domain` の `NarrationOutcome` を返すことはできません。

`TelemetryLogJson`（`TelemetryLogJson.kt`）は、3つの narrator feature が読み上げイベントをテレメトリログとして
保存する際に使う kotlinx.serialization の共通設定（`encodeDefaults` / `explicitNulls`）です。UDP/共有メモリ由来の
Float/Double フィールドが NaN/Infinity を取りうる GT7/ACE では、`Json(TelemetryLogJson) { allowSpecialFloatingPointValues
= true }` のように拡張して利用します。同ファイルの `String.toJsonStringLiteral()` は、`toString()` した状態オブジェクト
などをログ JSON の値としてそのまま埋め込むための文字列エスケープ関数です。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-narrator.svg)
<!-- MODULE-GRAPH-END -->
