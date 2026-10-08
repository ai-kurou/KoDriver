# domain

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/core-domain.svg)
<!-- MODULE-GRAPH-END -->

## 自由文言の試聴

`ReadoutPreviewGuard` は、空白文言・TTS利用不可・音量0以下の再生抑止とTTS利用可否の取得を共通化する。
`ReadoutTextPreviewHelper` はこの判定を使用し、項目の開始音完了後にTTSを再生する。
GT7の燃料残り周回数・燃料残量・タイヤ温度・自己ベストラップ、LMUのタイヤ温度・バーチャルエナジー残量・車両接近・ピットタイミング・自己ベストラップ・フラッグ、ACEのフラッグ・自己ベストラップの詳細ViewModelで使用する。
文言のプレースホルダー置換とサンプル値は各ViewModelが決め、解決済み文言と項目キーを渡す。
利用可否はViewModelのスコープで一度取得し、初期値falseで保持する。試聴はsuspend関数を同じスコープから呼び出し、キャンセル・例外は呼び出し元へ伝播する。

`ReadoutSpeechEventPreviewHelper` は同じ判定を使用し、LMUのタイヤ摩耗・ブレーキ温度・車両損傷の詳細ViewModelから受け取ったイベントを `PlaySpeechEventUseCase` へ渡す。
文言の置換とイベント生成は各ViewModelが担当し、イベント経由の開始音・TTS再生を維持する。

## ACEフラッグの読み上げ文言設定

`AceWindowsFlagReadoutTextPreferencesRepository` は `AceWindowsFlagReadoutTextKey` を指定する
`observeText` / `saveText` で、設定可能な10種類のフラッグ文言を取得・保存する。
フラッグなし・不明は設定対象に含めない。フラッグ別のUseCaseは対応するキーを指定し、
保存時の空白除去・最大文字数制限を維持する。
