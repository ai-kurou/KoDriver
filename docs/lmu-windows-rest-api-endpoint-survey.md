# Le Mans Ultimate REST API エンドポイント全数実測結果

[`lmu-windows-rest-api.md`](lmu-windows-rest-api.md) の付録。KoDriver では使っていないエンドポイントも含む、GET の全数実測結果と非 GET（書き込み系）エンドポイントの一覧。普段の実装では読まなくてよい。

## GET エンドポイント全数実測結果

`swagger-schema.json` に載っている GET 系 79 パスのうち、以下を除く **65 パス**を実機（Windows 機、LMU 起動中の `http://localhost:6397`）で実際に叩いた。

**除外したもの（14 パス）:**

- パスパラメータが必須で、有効な ID 等を都度取得しないと叩けないもの: `/rest/materialeditor/download/{materialGuid}` `/rest/materialeditor/{materialGuid}` `/rest/materialeditor/{materialGuid}/{map}` `/rest/race/car/{id}/image` `/rest/race/track/{id}/trackmap` `/rest/garage/setup/notes/(.*)` `/webdata/.*`
- レスポンスが画像・バイナリ等でテキストとして構造把握する意義が薄いもの: 上記の `.../image` 系
- 副作用がある（ゲームの状態や UI を変更しうる）ため、GET であっても意図的に叩かなかったもの: `/rest/multiplayer/join`（マルチプレイヤーへの参加を試みる）、`/rest/watch/play/{id}`（リプレイ再生を開始する）、`/rest/options/resetVRView`（VR ビューをリセットする）、`/rest/options/assign/changestatus`（入力デバイスの割り当て状態を変更する）
- 認証情報を含む可能性が高く、取得・記録を避けたもの: `/rest/profile/getAuthSessionTicket`（Steam 認証チケットを返すエンドポイント）

**実測結果一覧**（本ドキュメントの他セクションで詳細を記載済みの `weather`/`GetGameState`/`sessionInfo`/`standings`/`usage`/`RepairAndRefuel`/`getPlayerGarageData`/`pitstop-estimate`/`overall`/`PitMenu/receivePitMenu` は表中の「詳細」列で参照先を示す）。

実測時のステータスは、`/rest/garage/UIScreen/CoopOverview`（協力プレイ専用画面のため非協力プレイセッションでは **404**）を除き全て **200**。以下、カテゴリ別にフィールド単位で構造を記載する（キー名は実際のレスポンスのアルファベット順ではなく、実測 JSON の出現順）。

### `/navigation/...` `/rest/chat/`

**`/navigation/GetLoadingScreen`** — `selectedCar`（後述の `race/car` 1 件分と同一スキーマ: `desc`/`engine`/`fullPathTree`/`fullTeam`/`id`/`manufacturer`/`number`/`sig`/`team`/`vehFile`/`vehicle` 等）+ `trackInfo`（`race/track` に `corners`/`countryCode`/`defaultPracticeStartTime`/`defaultPracticeWeather`（5 要素の天候配列）/`defaultRaceLengthLaps`/`officialEvent`/`openingYear` 等のイベント既定値を加えた上位互換スキーマ、後述の `sessions/getTracksAll` と共通）。ロード画面に表示する車両・コース情報のスナップショット。

**`/navigation/getReferrer`** — `{"referrer": ""}` のみ。遷移元 URL/画面の識別子と推測されるが実測は空文字列。

**`/navigation/state`** — `loadingStatus`（`loading`(bool)/`loadingData`(内部状態を表す JSON 文字列、実測は `loading:false` のため空に近い断片)/`percentage`(実測 `-1` = ロード中でない)/`track`）+ `state`（`appBuild`(実測 `14130`、ビルド番号)/`gamePhase`/`gameSession`/`gameState`(`"GSTATE_DYN"`)/`internalStateCode`(`"OP_RAN_REALTIME_PASS"`)/`navigationState`(`"NAV_REALTIME"`)/`settingMode`(`"SETTING_GRANDPRIX"`)/`steamBetaBranchName`/`user`(`admin`(bool)/`userState`)）。アプリのビルド番号・内部状態遷移を把握できる、デバッグ用途寄りのエンドポイント。

**`/rest/chat/`** — `[]`。チャットメッセージの配列と推測されるが実測は未発言のため空。

### `/rest/garage/UIScreen/...`

**`/rest/garage/UIScreen/CarSetupOverview`** — `carPresetSetups.presets`（保存済みプリセットの配列、実測 3 件）、`carSetup.garageValues`（[`getPlayerGarageData`](lmu-windows-rest-api.md#restgaragegetplayergaragedata) と**同一のキー構造**の `VM_*`/`WM_*` オブジェクト。実装上同じデータソースを異なる画面向けに再利用していると推測される）、`currentWeather`（`airPressure`(実測 `99`)/`ambientTempKelvin`/`cloudCoverage`(0.0〜1.0)/`humidity`(0.0〜1.0)/`lightLevel`(0.0〜1.0)/`rainIntensity`/`raining`/`trackTempKelvin`。[`RepairAndRefuel.currentWeather`](lmu-windows-rest-api.md#restgarageuiscreenrepairandrefuel-のレスポンス構造)と同一スキーマ）、`racePosition`/`sessionTime`/`teamInfo`/`weatherForecast` は `RepairAndRefuel` と共通のサブ構造。

**`/rest/garage/UIScreen/CoopOverview`** — 実測は **404**（協力プレイでないセッションのため）。構造未確認。

**`/rest/garage/UIScreen/SessionSetup`** — `classesSelection`（選択中のクラス配列、実測 `["GTE"]`）、`fullGrid`(bool)、`selectedCar`/`trackInfo` は `GetLoadingScreen` と同一スキーマ。レースウィークエンド設定画面（クラス選択・グリッド）向け。

**`/rest/garage/UIScreen/TireManagement`** — 詳細は[「その他の実測エンドポイント」](lmu-windows-rest-api.md#その他の実測エンドポイント)を参照。上記に加え `pitMenu.pitMenu`（`RepairAndRefuel` と同型のピットメニュー全項目配列）、`wearables`（`body.aero`(数値)/`body.detachableParts`/`brakes`/`suspension`/`tires` の 4 輪配列、`RepairAndRefuel.wearables` と同一スキーマ）を含む。

### `/rest/garage/...`（タイヤ・車両状態・セットアップ管理）

**`/rest/garage/brakeinfo`** / **`/rest/garage/getVehicleCondition`** / **`/rest/garage/tireinfo`** / **`/rest/garage/UIScreen/TireManagement`** は[「その他の実測エンドポイント」](lmu-windows-rest-api.md#その他の実測エンドポイント)の各節に詳細を記載済み。

**`/rest/garage/isRefreshInProgress`** — `false`（bool のみ）。セットアップ一覧の再読み込み中かどうかのフラグ（`refreshSetups` POST と対）。

**`/rest/garage/setup`** — 保存済みセットアッププリセットの配列（実測 25 件）。各要素は `created`/`modified`（実測は空文字列 `""`。タイムスタンプ未設定 or 未実装の可能性）、`name`（プリセット名、実測に日本語コース名を含む例あり）、`numDiffUpgrades`（アップグレードパーツとの差分数）、`sameVehicleClass`（bool、現在の車両クラスと一致するプリセットかどうか）を持つ。

**`/rest/garage/showOnlyRelevantSetups`** — `false`（bool のみ）。

**`/rest/garage/summary`** — ガレージ画面全体のサマリー。`activeSetup`（適用中セットアップ名）、`activeSetupRawData`（16 進数文字列、内部エンコード済みセットアップデータと推測）、`car`（`displayProperties.displayName`/`fullTreePath` を含む `race/car` 相当のオブジェクト）、`compareToSetup`（比較対象セットアップ名、実測は空）、`currentTrackFolder`、`defaultSetup`（実測 `"<出荷時の設定>"`）、`fixedSetupRace`（bool、レースでセットアップ固定かどうか）、`track`（`race/track` 相当）、`unsavedChanges`（bool）に加えて、**`settingSummaries`** が特徴的: `AERODYNAMICS_FRONT`/`AERODYNAMICS_REAR`/`ENGINE`/`GEARS`/`SUSPENSION_FRONT`/`SUSPENSION_REAR`/`TIRES_FRONT`/`TIRES_REAR` のカテゴリ別に関連する `VM_*`/`WM_*` キーをグルーピングし、各カテゴリに `diffCount`（既定値からの変更項目数）を付与した集計ビュー。セットアップ UI のカテゴリタブ表示に対応すると推測される。

### `/rest/hud` `/rest/materialeditor/...` `/rest/multiplayer/...`

**`/rest/hud`** — `{"chat":true,"mfd":true,"speedo":true,"timing":true,"trackMap":true}`。HUD 各要素の表示 ON/OFF 状態（`POST /rest/hud/toggle/{component}` の `component` 名と一致）。

**`/rest/materialeditor/liveryeditor/getCustomSkinInfo`** — `albedo_texture`/`region_texture`。いずれも `data:image/png;base64,...` 形式の **Base64 埋め込み画像**（リバリーエディタのプレビューテクスチャ）。

**`/rest/multiplayer/join/state`** — `"JOIN_IDLE"`（文字列 enum。参加試行中は別の値になると推測されるが未確認）。

**`/rest/multiplayer/steam/status`** — `true`（bool。Steam 接続状態）。

**`/rest/multiplayer/teams`** — `null`（ソロセッションのため。マルチプレイ時にチーム編成情報が入ると推測）。

### `/rest/options/...`

**`/rest/options/UIScreen/Controls`** — `allControls.directInput`/`gamepad`/`keyboard` それぞれに `Device`/`Input`（デバイス別の割り当て一覧）+ `Type` を持ち、`global` にステアリング/フォースフィードバック等の共通設定（`Alternate Neutral Activation`/`Auto Reverse`/`Force Feedback`/`Steering Wheel Range` 等）を持つ、コントロール設定画面の全データ。

**`/rest/options/commandline`** — `{"commandLine": "\"...\\Le Mans Ultimate.exe\""}`。LMU 実行ファイルのローカルパスを含む（本ドキュメントには実際のフルパスを転記していない）。

**`/rest/options/display`** — `GAMEOPT_*`/`GRAPHOPT_*` の表示・グラフィック設定 26 キー。各値は共通スキーマ `{currentValue, maxValue, minValue, stepValue, stringValue, valueType}` を持つ（`stringValue` が表示用文字列、`valueType` は `"LONG"` 等）。例: `GAMEOPT_units`（`currentValue:0` → `"metric"`）、`GRAPHOPT_starting_view`（`currentValue:1` → `"Cockpit"`）。この `{currentValue/maxValue/minValue/stepValue/stringValue/valueType}` パターンは `/rest/options/settings`・`/rest/sessions/?` の値オブジェクトとも共通する定型フォーマット。

**`/rest/options/getAllHapticsDevices`** / **`/rest/options/getAllSoundDevices`** — 共通スキーマ: `Available`（`HardwareDevice`/`Name`/`OutputOnDevice`/`Renderer` を持つデバイス一覧、実測 Haptics 3 件・Sound 4 件）+ `Selected`（現在選択中のデバイス。実測は Haptics が `"Disabled"`、Sound が `"Default"`/`Renderer:"OpenAL Soft"`）。

**`/rest/options/getAllResolutions`** — 解像度候補の配列（実測 12 件）。各要素は `Height`/`Width`（ピクセル）+ `RefreshRate`（対応リフレッシュレートの配列、実測 6 件、Hz 単位と推測）。

**`/rest/options/getLanguage`** — `{"language": "japanese"}`。

**`/rest/options/liveInputs`** — `liveInputs.di`（DirectInput デバイスごとの生値の配列）、`gamepad`/`keyboard`（`minmax`/`raw inputs`）、**`processed inputs`**（`brakes`/`clutch`/`handbrake`/`handfrontbrake`/`steerLeft`/`steerRight`/`throttle`(数値、0.0〜1.0 と推測) + `directManualShift`/`launchControl`/`shiftDown`/`shiftToNeutral`/`shiftUp`/`tcOverride`(bool)）。**ライブのペダル/ステアリング入力値を返す**ため、共有メモリ以外からドライバー入力を取得できる数少ない経路。ポーリング頻度次第では入力デバイスのデバッグ・可視化に使える可能性がある。

**`/rest/options/occlusionCullingSupported`** — `{"occlusionCullingSupported": true}`。

**`/rest/options/settings`** — `/rest/options/display` と同じ `{currentValue/maxValue/minValue/stepValue/stringValue/valueType}` スキーマを持つ約 220 キーの巨大な設定スナップショット。プレフィックスの内訳（実測）は概ね `DRIVEAIDS_*`（運転支援。ABS/TC/オートブリップ等）、`GAMEOPT_*`（AI 強度・ダメージ倍率・自動保存等）、`GRAPHOPT_*`（画質・VR・HUD 表示詳細）、`SERVEROPT_*`（マルチプレイサーバー設定。ソロセッションでも既定値が存在）、`SOUNDOPT_*`（音量各種）、`VIDEOOPT_*`（解像度・vsync・アップスケール）、`CONTROL_*`/`COPY_*`/`DISPLAYOPT_*`/`MP_NETWORK_*`/`REPLAYOPT_*`。運転支援設定（`DRIVEAIDS_stability_control` 等）の現在値も含むため、コックピット内でのアシスト変更が実際にライブ反映されるかは別途要検証（[既知の注意点](lmu-windows-rest-api.md#既知の注意点落とし穴)の `getPlayerGarageData` 同様フリーズの可能性あり）。全 220 キーの個別列挙は情報量過多のため本ドキュメントでは省略し、プレフィックスからカテゴリを推測できる形に留める。

### `/rest/profile/...`

**`/rest/profile/`** — `name`/`nick`（表示名、実測は同一値）+ `steamID`（64bit SteamID64 文字列）。**Steam アカウント識別情報のため、本ドキュメントには実際の値を記載しない。**

**`/rest/profile/eacActive`** — `true`（Easy Anti-Cheat 有効化状態）。

**`/rest/profile/firstRun`** — `false`（初回起動フラグ）。

**`/rest/profile/inDevMode`** — `false`（開発者モードフラグ）。

**`/rest/profile/profileInfo/getProfileInfo`** — `/rest/profile/` に `nationality`（実測 `"JP"`）と `language`（実測 `"japanese"`）を加えた上位互換の構造。同様に SteamID を含むため実値は記載しない。

### `/rest/race/...` `/rest/replay/...`

**`/rest/race/car`** — インストール済み全車両の配列（実測 566 件、レスポンス約 44 万文字）。各要素は `displayProperties`（`displayName`/`fullTreePath`）、`dlcappID`、`engine`（実測例: `"Twin-turbo 4.0-litre V8"`）、`fullPathTree`（シリーズ名を含む階層パス文字列、実測例: `"ELMS 2025, GT3, Aston Martin Vantage AMR"`）、`id`（車両固有 ID）、`image`/`thumbnail`（画像を返す `/rest/race/car/{id}/image` への相対パス）、`manufacturer`、`name`、`owned`(bool)、`premId`、`sig`（署名/ハッシュ文字列）、`vehFile`（ローカルファイルパス）を持つ。

**`/rest/race/getAllowedToStartRacing`** — `true`（bool。レース開始条件を満たしているか）。

**`/rest/race/track`** — 全コースの配列（実測 37 件）。各要素は `displayProperties`（`name`/`shortName`）、`length`（文字列、km 単位、実測例 `"4.653"`）、`sceneDesc`（内部シーン識別子）、`trackLength`、`type`（実測 `"Road Course"`）等、`race/car` と対になる構造。

**`/rest/replay/CameraController/getCameraInfo`** — `{"cameraName":"COCKPIT","currentCameraGroup":"Driving"}`。現在の観戦/走行カメラ状態。

**`/rest/replay/isActive`** — `false`（bool。リプレイ再生中かどうか）。

### `/rest/sessions/...`（設定・イベント情報）

**`/rest/sessions/?`** — `SESSSET_*` 形式のレースウィークエンド設定、実測約 60 キー。値は `/rest/options/display` に近いが `{currentValue, numStepsTotal, settingID, stringValue, uiSelectionType, valueType}` という**別のスキーマ**（`uiSelectionType` が `"Arrow"`/`"Switch"` 等の UI 部品種別を表す点が特徴）。代表的なキー: `SESSSET_AI_Aggression`/`SESSSET_AI_Strength`（AI 強度）、`SESSSET_Damage_Multi`（ダメージ倍率、%）、`SESSSET_Finish_Criteria`（決勝の終了条件）、`SESSSET_Fuel_Usage`/`SESSSET_Tire_Wear`（燃料/タイヤ消費倍率、実測は両方 `"Realistic"`）、`SESSSET_Practice_Length`/`SESSSET_Qualify_Length`/`SESSSET_WarmUp_Length`（各セッション長、分単位）、`SESSSET_blue_flags`/`SESSSET_cut_rules`/`SESSSET_flag_rules`（フラッグ・カット判定ルール）、`SESSSET_pract1`〜`4`（各プラクティスセッションの有効/無効）、`SESSSET_pract1_realroad_init`等（各セッションのリアルロード＝路面グリップ初期状態プリセット）。全 60 キーの完全な列挙は情報量過多のため代表例に留める。

**`/rest/sessions/GetSessionsInfoForEvent`** — `{"scheduledSessions":[{"airTemp":20,"lengthTime":135,"name":"PRACTICE","rainChance":0}]}`。イベントで予定されているセッションの配列（実測はプラクティスのみの 1 件）。

**`/rest/sessions/SaveLoad/getSaveJSON`** — **セーブデータそのもの**に近い網羅的な構造（約 14 万文字）。主なフィールド: `RealRoad`（実測 `null`）、`SessionPreset`（`Grid`/`Player`（`DRIVER`/`Game Options`/`Mechanical Failures`/`Race Conditions`/`SCENE`）/`Weather`（`Practice`/`Qualifying`/`Race`）というプリセット全体）、`VehicleSetup`（実測は空文字列）、`Weather`（3 要素の配列、各要素が `Nodes`(内部データ文字列)/`unCompressedDataSize`/`unEncodedDataSize` を持つ圧縮天候データ）、`aiVehicles`（実測空配列）、`allowedVehiclesFilter`（`Optional`/`Required` の車両フィルタ）、`currentSession`（実測 `1`）、`endET`/`greenET`/`redLightET`/`startET`（セッション内の各種イベント経過時間、秒）、`gamePhase`（数値、`GetGameState.gamePhase` の文字列 enum とは異なる**数値表現**）、`maxLaps`（実測 `2147483647` = `int32` の最大値、無制限を表すセンチネル値と推測）、`playerVehicle.slotID`（実測 `-2`）、`sessionState`（数値 enum、実測 `8`）、`sessionTimescale`（実測 `1` = 等倍）、`startTime`/`timeOfDay`（実測どちらも `28800` 秒 = 8:00、`GetGameState.timeOfDay` と同系列だが値が異なりセーブ時点のスナップショット）、`uniqueSessionID`（セッション固有 ID、実測 10 桁の数値）。

**`/rest/sessions/amount`** — `{"PRACTICE":1,"QUALIFY":0,"RACE":0,"WARMUP":0}`（各セッション種別の設定数）。

**`/rest/sessions/getAllVehicles`** — インストール済み全車両×チーム構成の配列（実測 486 件）。`race/car` と多くのフィールドを共有しつつ、`classes`（クラス配列）、`classesOverride`、`drivers`（実測 6 要素、複数ドライバー編成に対応するエンデュランスチーム想定の配列）、`fullTeam`/`team`/`teamFounded`/`teamHeadquarters`（チーム詳細）、`isOwned`（`race/car` の `owned` と同義だが命名が異なる）、`number`（ゼッケン番号）を持つ、より参戦チーム目線の情報。

**`/rest/sessions/getTracksAll`** — 全コース情報の配列（実測 37 件）。`race/track` に `corners`（コーナー数、文字列）、`countryCode`、`defaultPracticeStartTime`/`defaultQualifyStartTime`/`defaultRaceStartTime`（既定開始時刻、分単位と推測）、`defaultPracticeWeather`/`defaultQualifyWeather`/`defaultRaceWeather`（各 5 要素の天候既定値配列）、`eventName`/`grandPrixName`、`location`、`officialEvent`(bool)、`openingYear` を加えたイベント既定値付き上位互換。

**`/rest/sessions/getTracksInSeries`** — 現在のシリーズで選択可能なコースの配列（実測 32 件）。スキーマは `getTracksAll` と同一。

**`/rest/sessions/opponents`** / **`/rest/sessions/opponents/all`** — `id`(number)/`name` のみを持つ軽量な対戦相手一覧（実測どちらも同じ 486 件）。両エンドポイントの違い（`all` が付く/付かない）は実測条件下では確認できず。

**`/rest/sessions/restartStintAvailable`** — `{"isRestartStintActionAvailable": false}`（bool。スティント再開始アクションの実行可否）。

### `/rest/watch/...`

**`/rest/watch/focus`** — `0`（number。現在の観戦フォーカス対象 `slotID`）。

**`/rest/watch/getBookmarkedTimestamps`** — `[]`（リプレイのブックマーク時刻一覧、未設定のため空配列）。

**`/rest/watch/replay/getReplayFolder`** — `custom`/`default` の 2 キー、いずれもリプレイ保存先のローカルディレクトリパス（実測では同一パス）。

**`/rest/watch/replays`** — 保存済みリプレイの配列（実測 242 件）。各要素は `id`（number）、`metadata`（`eventType`/`sceneDesc`/`session`）、`replayDirectory`（ローカルパス）、`replayName`（イベント名）、`size`（バイト数）、`timestamp`（Unix 時刻と推測される 10 桁の数値）。

**`/rest/watch/standings/history`** — `{"0": [...]}` のように**キーが `"0"` から始まる文字列インデックス**（実測はキー `"0"` のみ）を持つオブジェクトで、各値が `/rest/watch/standings` の簡略版（`carClass`/`driverName`/`finishStatus`/`lapTime`/`pitting`/`position`/`sectorTime1`/`sectorTime2`/`slotID`/`totalLaps`/`vehicleName`）の配列。キー `"0"` が「1 周目」を表すのか、それとも別のスナップショット単位かは実測（1 周未満のセッション）では判別できず、複数ラップ経過後にキーがどう増えるかも未確認。

**`/rest/watch/trackmap`** — コース形状の座標点配列（実測 1891 点）。各要素は `type`（number、実測 `0`。コーナー/ストレート等の区間種別と推測）、`x`/`y`/`z`（3 次元座標、共有メモリの `carPosition` と同じスケール感の数値）。

**`/rest/watch/getIncidentsList/{minTimeBetweenContacts}`** — `[]`（インシデント履歴。パラメータは接触判定の最小間隔秒数と推測、実測は `0` を指定し空配列が返った。接触イベントが発生した場合の要素構造は未確認）。

---

## 非 GET（書き込み系）エンドポイント一覧

> **本セクションは実機での呼び出し結果ではなく、`swagger-schema.json` の定義（パス・メソッド・パラメータ）のみを整理したものである。** 実機呼び出しを試みた際に LMU がクラッシュする事象が発生したため（詳細は上記の限界の記載を参照）、書き込み系エンドポイントは意図的に**実行していない**。KoDriver は読み取り専用アプリであり、これらのエンドポイントを実装で使う予定はないが、REST API の全体像を把握するための参考情報として一覧化する。

`swagger-schema.json` の非 GET（POST/PUT/DELETE）は 107 パス・108 メソッド定義（`/rest/garage/setup` のみ POST と PUT の 2 定義を持つ）。カテゴリ別に、パス・メソッド・パスパラメータ／ボディの有無と、パス名・既存の GET 系調査から推測される用途をまとめる。**「推測用途」はパス名からの推測であり実機検証はしていない点に注意。**

### `/navigation/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/navigation/action/{action}` | POST | パス: `action` | UI ナビゲーションアクションの実行（戻る等） |
| `/navigation/openLiveryEditor` | POST | なし | リバリーエディタ画面を開く |
| `/navigation/sendToLog` | POST | ボディ | ゲーム内ログへの書き込み |
| `/navigation/setReferrer` | POST | ボディ | `/navigation/getReferrer` が返す参照元情報の設定 |

### `/rest/cancelSteamAuth` `/rest/chat/`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/cancelSteamAuth` | POST | なし | Steam 認証フローのキャンセル |
| `/rest/chat/` | POST | ボディ | チャットメッセージ送信 |

### `/rest/garage/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/garage/` | PUT | ボディ | ガレージ状態の一括更新 |
| `/rest/garage/(VM_.*)` | POST | パス: `mod`（正規表現）、ボディ | `VM_*`（車両全体セットアップ項目）の値変更。CarSetupOverview 等の UI 操作用 |
| `/rest/garage/(WM_.*)-(.*)` | POST | パス: `mod`/`wheel`（正規表現）、ボディ | `WM_*`（4 輪個別項目）の値変更（例: タイヤ圧・キャンバー） |
| `/rest/garage/PitMenu/loadPitMenu` | POST | なし | ピットメニュー状態の読み込み |
| `/rest/garage/SetCurrentVehicle` | POST | ボディ | 現在選択中の車両の設定 |
| `/rest/garage/SetPreviewSaveFile` | POST | ボディ | プレビュー用セーブファイルの指定 |
| `/rest/garage/drive` | POST | なし | ガレージから走行を開始する（コースイン） |
| `/rest/garage/refreshSetups` | POST | ボディ | 保存済みセットアップ一覧の再読み込み |
| `/rest/garage/setup` | POST | ボディ | セットアップの新規保存 |
| `/rest/garage/setup` | PUT | ボディ | セットアップの上書き保存 |
| `/rest/garage/setup/(.*)` | **DELETE** | パス: `setup`（正規表現） | 指定したセットアップの削除（**恒久的なデータ削除に該当するため実行対象外**） |
| `/rest/garage/setup/compare` | POST | ボディ | セットアップ比較 |
| `/rest/garage/setup/default` | POST | なし | セットアップを既定値にリセット |
| `/rest/garage/setup/notes` | POST | ボディ | セットアップのメモ保存 |
| `/rest/garage/showOnlyRelevantSetups` | POST | ボディ | 「関連セットアップのみ表示」フラグの変更（GET 版と対） |
| `/rest/garage/toRaceMenu` | POST | なし | レースメニューへ遷移 |

### `/rest/hud/...` `/rest/liveryeditor/...` `/rest/materialeditor/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/hud/toggle/{component}` | POST | パス: `component` | HUD 個別要素（`speedo`/`chat`/`mfd`/`timing`/`trackMap` 等）の表示切り替え |
| `/rest/hud/toggleAllComponents/{visible}` | POST | パス: `visible`(bool) | HUD 全要素の表示・非表示一括切り替え |
| `/rest/liveryeditor/setCamera/{camera}` | POST | パス: `camera` | リバリーエディタのカメラ切り替え |
| `/rest/liveryeditor/showRegionTexture/{active}` | POST | パス: `active`(bool) | リバリーエディタの領域テクスチャ表示切り替え |
| `/rest/liveryeditor/submitCustomSkin` | POST | なし | カスタムスキンの適用申請 |
| `/rest/materialeditor/liveryeditor/reloadCustomSkin` | POST | なし | カスタムスキンの再読み込み |
| `/rest/materialeditor/{materialGuid}` | PUT | パス: `materialGuid`、ボディ | マテリアル定義の更新 |
| `/rest/materialeditor/{materialGuid}/persist` | POST | パス: `materialGuid`、ボディ | マテリアル定義の永続化保存 |
| `/rest/materialeditor/{materialGuid}/shader` | PUT | パス: `materialGuid`、ボディ | マテリアルのシェーダー設定更新 |

### `/rest/multiplayer/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/multiplayer/cancelJoinRequest` | POST | なし | マルチプレイヤー参加リクエストのキャンセル |
| `/rest/multiplayer/exitVehicle` | POST | なし | 車両からの降車（観戦モードへの移行等） |
| `/rest/multiplayer/takeControlOfVehicle` | POST | なし | 車両の操作権を取得 |

### `/rest/options/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/options/ApplyVideoOptions` | POST | なし | 変更したビデオ設定の適用 |
| `/rest/options/assign/cancel` | POST | なし | 入力デバイス割り当てのキャンセル |
| `/rest/options/assign/confirm` | POST | なし | 入力デバイス割り当ての確定 |
| `/rest/options/float` | POST | ボディ | float 型オプション値の設定（汎用） |
| `/rest/options/graphics/confirmgraphics` | POST | なし | グラフィック設定変更の確定 |
| `/rest/options/graphics/resetgraphics` | POST | なし | グラフィック設定を既定値にリセット |
| `/rest/options/long` | POST | ボディ | 整数型オプション値の設定（汎用） |
| `/rest/options/setConfigControl` | POST | ボディ | キー/ボタン割り当ての設定 |
| `/rest/options/setControls` | POST | ボディ | コントロール設定全般の保存 |
| `/rest/options/setHapticsDevice` | POST | クエリ: `Name` | ハプティクスデバイスの選択 |
| `/rest/options/setInMenuGfxEffects` | PUT | ボディ | メニュー内グラフィックエフェクトの設定 |
| `/rest/options/setInputAxisProperties` | POST | ボディ | 入力軸（ステアリング等）のプロパティ設定 |
| `/rest/options/setSoundDevice` | POST | クエリ: `Name` | サウンドデバイスの選択 |
| `/rest/options/unsetConfigControl` | POST | ボディ | キー/ボタン割り当ての解除 |

### `/rest/profile/...` `/rest/race/...` `/rest/replay/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/profile/DLC/viewDLC` | POST | なし | DLC 購入ページ（Steam ストア等）を開く |
| `/rest/profile/profileInfo/setProfileInfo` | POST | ボディ | プロフィール情報（表示名・国籍等）の更新 |
| `/rest/race/startRace` | POST | なし | レース（セッション）の開始 |
| `/rest/race/track` | POST | ボディ | コース情報の登録・更新 |
| `/rest/replay/CameraController/setCamera` | POST | ボディ | 観戦カメラの切り替え |
| `/rest/replay/toggleactive` | POST | なし | リプレイコントローラーの有効/無効切り替え |

### `/rest/sessions/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/sessions/Championship/getCurrentChampTemplate` | POST | なし | 現在のチャンピオンシップテンプレート取得 |
| `/rest/sessions/Championship/getGrid` | POST | なし | チャンピオンシップのグリッド取得 |
| `/rest/sessions/Championship/setCurrentChampionshipTemplate` | POST | ボディ | チャンピオンシップテンプレートの設定 |
| `/rest/sessions/Coop/setCoopDriverID` | POST | ボディ | 協力プレイのドライバー ID 設定 |
| `/rest/sessions/FFtoRaceEnd` | POST | なし | セッションをレース終了までファストフォワード |
| `/rest/sessions/MultiStintRace/Drive` | POST | なし | マルチスティントレースでの走行開始 |
| `/rest/sessions/MultiStintRace/UnPause` | POST | なし | マルチスティントレースの一時停止解除 |
| `/rest/sessions/MultiStintRace/setDriverInfo` | POST | ボディ | マルチスティントのドライバー情報設定 |
| `/rest/sessions/SaveLoad/compressSaveFile` | POST | なし | セーブファイルの圧縮 |
| `/rest/sessions/SaveLoad/decompressSaveFile` | POST | なし | セーブファイルの展開 |
| `/rest/sessions/SaveLoad/deleteSaveFile` | POST | なし | セーブファイルの削除（**恒久的なデータ削除に該当するため実行対象外**） |
| `/rest/sessions/SaveLoad/doesBackupExistForThisSession` | POST | なし | 現セッションのバックアップ存在確認 |
| `/rest/sessions/SaveLoad/generateSaveFileFromSessionPreset` | POST | ボディ | セッションプリセットからのセーブファイル生成 |
| `/rest/sessions/SaveLoad/getEveryLocalSave` | POST | なし | ローカルセーブファイル一覧取得 |
| `/rest/sessions/SaveLoad/getNumSaves` | POST | なし | セーブファイル数取得 |
| `/rest/sessions/SaveLoad/isSaveNameValid` | POST | ボディ | セーブ名のバリデーション |
| `/rest/sessions/SaveLoad/loadGame` | POST | なし | セーブデータのロード |
| `/rest/sessions/SaveLoad/saveGame` | POST | なし | 現在の状態をセーブ |
| `/rest/sessions/SaveLoad/saveLastBackup` | POST | なし | 直近バックアップの保存 |
| `/rest/sessions/SaveLoad/saveTemplateToFile` | POST | なし | テンプレートのファイル保存 |
| `/rest/sessions/SessionPresets/applyPreset` | POST | なし | セッションプリセットの適用 |
| `/rest/sessions/SessionPresets/getDefaultPresetForTrack` | POST | なし | コースの既定プリセット取得 |
| `/rest/sessions/SessionPresets/requestPreset` | POST | なし | プリセットのリクエスト |
| `/rest/sessions/ai/TakeDriverControl` | POST | なし | AI にドライバー操作を引き渡す |
| `/rest/sessions/ai/forcePlayerVehAiPit` | POST | なし | プレイヤー車両を強制的に AI 操作でピットへ |
| `/rest/sessions/clearEventNotification` | POST | ボディ | イベント通知のクリア |
| `/rest/sessions/continueGame` | POST | なし | ゲームの続行（ポーズ解除等） |
| `/rest/sessions/getAllAvailableVehicles` | POST | ボディ | 選択可能な車両一覧取得（フィルタ条件付き） |
| `/rest/sessions/notifyInPauseSettings` | POST | ボディ | ポーズ中設定変更の通知 |
| `/rest/sessions/playVOTrigger` | POST | なし | ボイスオーバー（アナウンス音声）トリガーの再生 |
| `/rest/sessions/playerSettings/backupPlayerSettings` | POST | なし | プレイヤー設定のバックアップ |
| `/rest/sessions/playerSettings/restorePlayerSettingsFromBackup` | POST | なし | バックアップからのプレイヤー設定復元 |
| `/rest/sessions/raceControlVerification` | POST | なし | レースコントロールの検証 |
| `/rest/sessions/restartStintAvailable` | POST | なし | スティント再開始可否フラグの設定（GET 版と対） |
| `/rest/sessions/resumePitStop` | POST | なし | ピットストップ処理の再開 |
| `/rest/sessions/returnToMonitor` | POST | なし | 観戦モニター画面へ戻る |
| `/rest/sessions/saveload/getSaveFileJSONFromFilename` | POST | ボディ | 指定ファイル名のセーブデータ JSON 取得 |
| `/rest/sessions/setEventNotification` | POST | ボディ | イベント通知の設定 |
| `/rest/sessions/setHudOnWatchScreen` | POST | ボディ | 観戦画面での HUD 表示設定 |
| `/rest/sessions/settings` | POST | ボディ | セッション設定（`SESSSET_*`、GET `/rest/sessions/?` 相当）の一括更新 |
| `/rest/sessions/weather/{session}/{node}/{setting}` | POST | パス: `session`/`node`/`setting`、ボディ | 天候予報ノードの個別設定値変更（プラクティス UI 操作用） |
| `/rest/sessions/weather/{session}/{preset}` | POST | パス: `session`/`preset` | 天候プリセットの適用 |
| `/rest/sessions/{session}/sessions` | POST | パス: `session` | セッション構成（プラクティス/予選/レース数等）の変更 |

### `/rest/start/...` `/rest/watch/...` `/webdata/...`

| パス | メソッド | パラメータ | 推測用途 |
|---|---|---|---|
| `/rest/start/openExternalBrowserToURL` | POST | ボディ | 既定の外部ブラウザで指定 URL を開く |
| `/rest/watch/focus/{cameraType}/{trackSideGroup}/{shouldAdvance}` | PUT | パス: `cameraType`/`trackSideGroup`(int)/`shouldAdvance`(bool) | 観戦カメラのフォーカス対象を条件指定で変更 |
| `/rest/watch/focus/{slotid}` | PUT | パス: `slotid`(int) | 観戦フォーカス対象をスロット ID で指定（GET `/rest/watch/focus` と対） |
| `/rest/watch/focusBackward` | PUT | なし | 観戦フォーカスを前の車両へ |
| `/rest/watch/focusForward` | PUT | なし | 観戦フォーカスを次の車両へ |
| `/rest/watch/replay/setCurrentMetadata` | PUT | ボディ | リプレイのメタデータ設定 |
| `/rest/watch/replay/setReplayFolder` | PUT | ボディ | リプレイ保存先フォルダの設定 |
| `/rest/watch/replay/setReplayUIVisible` | POST | ボディ | リプレイ UI の表示切り替え |
| `/rest/watch/replayCommand/{command}` | PUT | パス: `command` | リプレイ操作コマンド（再生/一時停止等）の実行 |
| `/rest/watch/replaytime/{time}` | PUT | パス: `time`(number) | リプレイ再生位置（時刻）の指定 |
| `/webdata/.*` | POST | ワイルドカード | 汎用 Web データエンドポイント（用途不明） |
