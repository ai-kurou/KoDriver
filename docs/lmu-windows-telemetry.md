# Le Mans Ultimate 内蔵共有メモリ（LMU_Data）フィールドリファレンス

Le Mans Ultimate（LMU）は Studio 397 純正の共有メモリインターフェースを内蔵しており、共有メモリ名 **`LMU_Data`** で全データを単一セグメントとして公開する。構造体定義の正典はゲームインストールフォルダの `Support\SharedMemoryInterface` にあるヘッダファイル（`SharedMemoryInterface.hpp` / `InternalsPlugin.hpp`）であり、本ドキュメントはその Python 移植である [TinyPedal/pyLMUSharedMemory](https://github.com/TinyPedal/pyLMUSharedMemory) の `lmu_data.py` を基に、ctypes（`_pack_=4`）で算出した実オフセットを記載する。

## rF2SharedMemoryMapPlugin との関係

rFactor 2 で使われる [TheIronWolfModding/rF2SharedMemoryMapPlugin](https://github.com/TheIronWolfModding/rF2SharedMemoryMapPlugin) は `$rFactor2SMMP_Telemetry$` / `$rFactor2SMMP_Scoring$` など機能別の複数セグメントを公開する**サードパーティプラグイン**であり、LMU 内蔵の `LMU_Data` とは別物である。ただし、内部の車両テレメトリ（`TelemInfoV01`）・スコアリング（`ScoringInfoV01` / `VehicleScoringInfoV01`）構造体はどちらも rFactor 2 エンジンの `InternalsPlugin.hpp` に由来するため、多くのフィールド名・意味は共通している。一方で LMU 版は末尾に LMU 固有フィールド（バーチャルエナジー、TC/ABS 車載設定、ギャップ情報など）が追加されており、配列サイズやオフセットも rF2 プラグイン版とは一致しない。

rF2 プラグイン側にのみ存在する構造体（`rF2Rules`, `rF2MultiRules`, `rF2ForceFeedback`, `rF2Graphics`, `rF2PitInfo`, `rF2Weather`, `rF2Extended` など）は **`LMU_Data` には含まれない**。

---

## LMUObjectOut レイアウト（全体 324,820 bytes）

`LMU_Data` の先頭から以下の順に配置される。すべて `_pack_=4` アライメント。

| セグメント | 元の構造体名 | 先頭オフセット | サイズ |
|---|---|---|---|
| `generic`（LMUGeneric） | SharedMemoryGeneric | 0 | 332 bytes |
| `paths`（LMUPathData） | SharedMemoryPathData | 332 | 1,300 bytes |
| `scoring`（LMUScoringData） | SharedMemoryScoringData | 1,632 | 126,832 bytes |
| `telemetry`（LMUTelemetryData） | SharedMemoryTelemtryData | 128,464 | 196,356 bytes |

車両配列の最大数は **104**（`MAX_MAPPED_VEHICLES`）。

---

## 基本型

### LMUVect3（TelemVect3、24 bytes）

| フィールド | 型 | オフセット（相対） | 説明 |
|---|---|---|---|
| `x` | double | +0 | X 軸成分 |
| `y` | double | +8 | Y 軸成分（上方向） |
| `z` | double | +16 | Z 軸成分 |

---

## LMUGeneric（汎用情報、オフセット 0、332 bytes）

| フィールド | 型 | オフセット（絶対） | 説明 |
|---|---|---|---|
| `events` | LMUEvent | 0 | イベントカウンタ群（uint32×16。SME_ENTER, SME_EXIT, SME_STARTUP, SME_SHUTDOWN, SME_LOAD, SME_UNLOAD, SME_START_SESSION, SME_END_SESSION, SME_ENTER_REALTIME, SME_EXIT_REALTIME, SME_UPDATE_SCORING, SME_UPDATE_TELEMETRY, SME_INIT_APPLICATION, SME_UNINIT_APPLICATION, SME_SET_ENVIRONMENT, SME_FFB） |
| `gameVersion` | int32 | 64 | ゲームバージョン |
| `FFBTorque` | float | 68 | FFB トルク |
| `appInfo` | LMUApplicationState | 72 | アプリケーション状態（下表） |

### LMUApplicationState（ApplicationStateV01、260 bytes）

| フィールド | 型 | オフセット（相対） | 説明 |
|---|---|---|---|
| `mAppWindow` | uint64 (HWND) | +0 | アプリウィンドウハンドル |
| `mWidth` | uint32 | +8 | 画面幅 |
| `mHeight` | uint32 | +12 | 画面高さ |
| `mRefreshRate` | uint32 | +16 | リフレッシュレート |
| `mWindowed` | uint32 | +20 | ウィンドウモードか（実質 bool） |
| `mOptionsLocation` | uint8 | +24 | 0=メイン UI, 1=トラックロード中, 2=モニター, 3=走行中 |
| `mOptionsPage[31]` | char[31] | +25 | オプションページ名 |
| `mExpansion[204]` | uint8[204] | +56 | 将来拡張用 |

---

## LMUPathData（パス情報、オフセット 332、1,300 bytes）

各フィールドは `char[260]`（Windows の MAX_PATH）。

| フィールド | オフセット（絶対） | 説明 |
|---|---|---|
| `userData` | 332 | UserData フォルダパス |
| `customVariables` | 592 | カスタム変数ファイルパス |
| `stewardResults` | 852 | スチュワード結果ファイルパス |
| `playerProfile` | 1112 | プレイヤープロファイルパス |
| `pluginsFolder` | 1372 | プラグインフォルダパス |

---

## LMUScoringData（スコアリング、オフセット 1,632、126,832 bytes）

| フィールド | 型 | オフセット（絶対） | サイズ |
|---|---|---|---|
| `scoringInfo` | LMUScoringInfo | 1,632 | 548 bytes |
| `scoringStreamSize` | uint8[12] | 2,180 | 12 bytes |
| `vehScoringInfo[104]` | LMUVehicleScoring×104 | 2,192 | 584 bytes × 104 |
| `scoringStream` | char[65536] | 63,928 | 65,536 bytes |

### LMUScoringInfo（ScoringInfoV01、レース全体情報）

`SCORING_BASE = 1632` からの絶対オフセットで記載する。

| フィールド | 型 | オフセット（絶対） | 単位 | 説明 |
|---|---|---|---|---|
| `mTrackName[64]` | char[64] | 1632+0 | — | サーキット名 |
| `mSession` | int32 | 1632+64 | enum | セッション種別（下表参照） |
| `mCurrentET` | double | 1632+68 | s | 現在のセッション経過時間 |
| `mEndET` | double | 1632+76 | s | セッション終了時刻 |
| `mMaxLaps` | int32 | 1632+84 | — | 最大ラップ数 |
| `mLapDist` | double | 1632+88 | m | サーキット1周の距離 |
| `mResultsStreamPointer` | uint8[8] | 1632+96 | — | （ポインタ）結果ストリーム。共有メモリ経由では無意味 |
| `mNumVehicles` | int32 | 1632+104 | — | 現在の車両数 |
| `mGamePhase` | uint8 | 1632+108 | enum | ゲームフェーズ（下表参照） |
| `mYellowFlagState` | int8 | 1632+109 | enum | イエローフラッグ状態（下表参照、フルコースのみ） |
| `mSectorFlag[3]` | uint8[3] | 1632+110 | — | セクターごとのローカルイエロー |
| `mStartLight` | uint8 | 1632+113 | — | スタートライト（フレーム番号、数はトラック依存） |
| `mNumRedLights` | uint8 | 1632+114 | — | スタートシーケンスの赤ライト数 |
| `mInRealtime` | bool | 1632+115 | — | リアルタイム（走行中）か、モニター画面か |
| `mPlayerName[32]` | char[32] | 1632+116 | — | プレイヤー名 |
| `mPlrFileName[64]` | char[64] | 1632+148 | — | プレイヤーファイル名 |
| `mDarkCloud` | double | 1632+212 | 0.0–1.0 | 雲の暗さ |
| `mRaining` | double | 1632+220 | 0.0–1.0 | 降雨強度 |
| `mAmbientTemp` | double | 1632+228 | ℃ | 大気温度 |
| `mTrackTemp` | double | 1632+236 | ℃ | 路面温度 |
| `mWind` | LMUVect3 | 1632+244 | m/s | 風速ベクトル |
| `mMinPathWetness` | double | 1632+268 | 0.0–1.0 | 走行ライン上の最小湿潤度 |
| `mMaxPathWetness` | double | 1632+276 | 0.0–1.0 | 走行ライン上の最大湿潤度 |
| `mGameMode` | uint8 | 1632+284 | — | 1=サーバー, 2=クライアント, 3=両方 |
| `mIsPasswordProtected` | bool | 1632+285 | — | サーバーパスワード保護 |
| `mServerPort` | uint16 | 1632+286 | — | サーバーポート番号 |
| `mServerPublicIP` | uint32 | 1632+288 | — | サーバーパブリック IP |
| `mMaxPlayers` | int32 | 1632+292 | — | セッション最大車両数 |
| `mServerName[32]` | char[32] | 1632+296 | — | サーバー名 |
| `mStartET` | float | 1632+328 | s（深夜0時からの秒数） | イベント開始時刻 |
| `mAvgPathWetness` | double | 1632+332 | 0.0–1.0 | 走行ライン上の平均湿潤度 |
| `mSessionTimeRemaining` | float | 1632+340 | s | セッション残り時間 |
| `mTimeOfDay` | float | 1632+344 | — | ゲーム内時刻 |
| `mIsFixedSetup` | bool | 1632+348 | — | 固定セットアップか |
| `mTrackGripLevel` | uint8 | 1632+349 | enum | ラバーグリップレベル（下表参照、雨で流される） |
| `mCloudCoverage` | uint8 | 1632+350 | enum | 空模様（下表参照） |
| `mTrackLimitsStepsPerPenalty` | uint8 | 1632+351 | — | ペナルティ 1 回あたりのトラックリミットステップ数 |
| `mTrackLimitsStepsPerPoint` | uint8 | 1632+352 | — | ポイント 1 つあたりのトラックリミットステップ数 |
| `mExpansion[187]` | uint8[187] | 1632+353 | — | 将来拡張用 |
| `mVehiclePointer` | uint8[8] | 1632+540 | — | （ポインタ）共有メモリ経由では無意味 |

### mSession 列挙値

| 値 | 意味 |
|---|---|
| 0 | Test Day（テスト走行） |
| 1–4 | Practice（練習走行） |
| 5–8 | Qualifying（予選） |
| 9 | Warmup（ウォームアップ） |
| 10–13 | Race（決勝） |

### mGamePhase 列挙値

| 値 | 意味 |
|---|---|
| 0 | セッション開始前 |
| 1 | Reconnaissance laps（レースのみ） |
| 2 | GridWalk（グリッドウォーク、レースのみ） |
| 3 | Formation（フォーメーションラップ、レースのみ） |
| 4 | Countdown（スタートライト点灯開始、レースのみ） |
| 5 | GreenFlag（グリーンフラッグ） |
| 6 | FullCourseYellow（FCY / セーフティカー） |
| 7 | SessionStopped（セッション停止） |
| 8 | SessionOver（セッション終了） |
| 9 | Paused（ポーズ中。プラグインへのハートビート呼び出し） |

### mYellowFlagState 列挙値

| 値 | 意味 |
|---|---|
| -1 | Invalid |
| 0 | None（グリーン） |
| 1 | Pending（FCY 発動保留） |
| 2 | PitClosed（ピットクローズ） |
| 3 | PitLeadLap（先頭周回のみピット可） |
| 4 | PitOpen（ピットオープン） |
| 5 | LastLap（最終周） |
| 6 | Resume（リスタート） |
| 7 | RaceHalt（レース中断、現在未使用） |

### mTrackGripLevel 列挙値

| 値 | 意味 |
|---|---|
| 0 | Green（ラバーなし） |
| 1 | Low |
| 2 | Medium |
| 3 | High (Heavy) |
| 4 | Saturated（飽和） |

### mCloudCoverage 列挙値

| 値 | 意味 |
|---|---|
| 0 | 快晴 |
| 1 | 薄曇り |
| 2 | 部分的に曇り |
| 3 | ほぼ曇り |
| 4 | 曇天 |
| 5 | 曇り＋霧雨 |
| 6 | 曇り＋小雨 |
| 7 | 曇天＋小雨 |
| 8 | 曇天＋雨 |
| 9 | 曇天＋大雨 |
| 10 | 曇天＋嵐 |

---

## LMUVehicleScoring（VehicleScoringInfoV01、各車両のレース状態）

`vehScoringInfo[104]` の各要素。先頭オフセット **2,192**、stride **584 bytes**（n 台目の絶対オフセット = `2192 + n × 584`）。

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mID` | int32 | +0 | — | スロット ID（マルチプレイでは離脱後に再利用されうる） |
| `mDriverName[32]` | char[32] | +4 | — | ドライバー名 |
| `mVehicleName[64]` | char[64] | +36 | — | 車両名 |
| `mTotalLaps` | int16 | +100 | — | 完了ラップ数 |
| `mSector` | int8 | +102 | — | 現在のセクター（**0=S3, 1=S1, 2=S2**） |
| `mFinishStatus` | int8 | +103 | — | 0=未完走, 1=完走, 2=DNF, 3=DQ |
| `mLapDist` | double | +104 | m | サーキット上の現在位置 |
| `mPathLateral` | double | +112 | m | センターラインからの横方向距離 |
| `mTrackEdge` | double | +120 | m | 同じ側のトラックエッジまでの距離 |
| `mBestSector1` | double | +128 | s | ベストセクター1タイム |
| `mBestSector2` | double | +136 | s | ベストセクター2累積タイム（S1+S2） |
| `mBestLapTime` | double | +144 | s | ベストラップタイム |
| `mLastSector1` | double | +152 | s | 前周のセクター1タイム |
| `mLastSector2` | double | +160 | s | 前周のセクター2累積タイム |
| `mLastLapTime` | double | +168 | s | 前周のラップタイム |
| `mCurSector1` | double | +176 | s | 現在周のセクター1タイム（有効な場合） |
| `mCurSector2` | double | +184 | s | 現在周のセクター2累積タイム（有効な場合） |
| `mNumPitstops` | int16 | +192 | — | ピットストップ回数 |
| `mNumPenalties` | int16 | +194 | — | 未消化ペナルティ数 |
| `mIsPlayer` | bool | +196 | — | プレイヤーの車両か |
| `mControl` | int8 | +197 | — | -1=なし, 0=プレイヤー, 1=AI, 2=リモート, 3=リプレイ |
| `mInPits` | bool | +198 | — | ピットレーン走行中（リモート車両は不正確な場合あり） |
| `mPlace` | uint8 | +199 | — | 順位（1ベース） |
| `mVehicleClass[32]` | char[32] | +200 | — | クラス名 |
| `mTimeBehindNext` | double | +232 | s | 次順位の車両との時間差 |
| `mLapsBehindNext` | int32 | +240 | — | 次順位の車両との周回差 |
| `mTimeBehindLeader` | double | +244 | s | トップとの時間差 |
| `mLapsBehindLeader` | int32 | +252 | — | トップとの周回差 |
| `mLapStartET` | double | +256 | s | 現在ラップ開始セッション時刻 |
| `mPos` | LMUVect3 | +264 | m | ワールド座標 |
| `mLocalVel` | LMUVect3 | +288 | m/s | ローカル速度 |
| `mLocalAccel` | LMUVect3 | +312 | m/s² | ローカル加速度 |
| `mOri[3]` | LMUVect3[3] | +336 | — | 姿勢行列 |
| `mLocalRot` | LMUVect3 | +408 | rad/s | ローカル回転速度 |
| `mLocalRotAccel` | LMUVect3 | +432 | rad/s² | ローカル回転加速度 |
| `mHeadlights` | uint8 | +456 | — | ヘッドライト状態 |
| `mPitState` | uint8 | +457 | — | 0=なし, 1=要求, 2=入場中, 3=停止中, 4=出場中 |
| `mServerScored` | uint8 | +458 | — | サーバーによってスコアリングされているか |
| `mIndividualPhase` | uint8 | +459 | — | 個別のゲームフェーズ（0–8 + 9=フォーメーション後, 10=イエロー下, 11=ブルー下〈未使用〉） |
| `mQualification` | int32 | +460 | — | 予選順位（1ベース、無効時は-1） |
| `mTimeIntoLap` | double | +464 | s | 現在ラップ内の推定経過時間 |
| `mEstimatedLapTime` | double | +472 | s | 推定ラップタイム（time behind / time into lap の計算に使用） |
| `mPitGroup[24]` | char[24] | +480 | — | ピットグループ名（ピット共有時以外はチーム名と同じ） |
| `mFlag` | uint8 | +504 | — | この車両への提示フラッグ（現状 0=グリーン, 6=ブルーのみ） |
| `mUnderYellow` | bool | +505 | — | FCY をスタート/フィニッシュラインで受けたか |
| `mCountLapFlag` | uint8 | +506 | — | 0=カウントなし, 1=ラップのみ, 2=ラップ+タイム |
| `mInGarageStall` | bool | +507 | — | 正しいガレージストール内にいるか |
| `mUpgradePack[16]` | uint8[16] | +508 | — | アップグレードパック情報（エンコード済み） |
| `mPitLapDist` | float | +524 | m | ピットロケーションのラップ距離位置 |
| `mBestLapSector1` | float | +528 | s | ベストラップ時のセクター1タイム（ベストセクター1とは限らない） |
| `mBestLapSector2` | float | +532 | s | ベストラップ時のセクター1+2累積タイム（ベストセクター2とは限らない） |
| `mSteamID` | uint64 | +536 | — | 現在のドライバーの SteamID（あれば） |
| `mVehFilename[32]` | char[32] | +544 | — | 車両識別用 veh ファイル名 |
| `mAttackMode` | int16 | +576 | — | アタックモード |
| `mFuelFraction` | uint8 | +578 | 0x00–0xFF | 燃料/バッテリー残量割合（0x00=0%, 0xFF=100%） |
| `mDRSState` | bool | +579 | — | DRS（リアフラップ）状態 |
| `mExpansion[4]` | uint8[4] | +580 | — | 将来拡張用 |

---

## LMUTelemetryData（テレメトリ、オフセット 128,464、196,356 bytes）

| フィールド | 型 | オフセット（絶対） | 説明 |
|---|---|---|---|
| `activeVehicles` | uint8 | 128464+0 | アクティブ車両数 |
| `playerVehicleIdx` | uint8 | 128464+1 | プレイヤー車両の `telemInfo` インデックス |
| `playerHasVehicle` | bool | 128464+2 | プレイヤーが車両を持っているか |
| `telemInfo[104]` | LMUVehicleTelemetry×104 | 128464+4 | 車両テレメトリ配列（stride 1,888 bytes） |

n 台目の車両先頭（`vehicleBase`）= `128464 + 4 + n × 1888`。

### LMUVehicleTelemetry（TelemInfoV01、1,888 bytes）

#### 識別・時間

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mID` | int32 | +0 | — | スロット ID |
| `mDeltaTime` | double | +4 | s | 前回更新からの経過時間 |
| `mElapsedTime` | double | +12 | s | セッション開始からの経過時間 |
| `mLapNumber` | int32 | +20 | — | 現在のラップ番号 |
| `mLapStartET` | double | +24 | s | 現在ラップ開始時のセッション時間 |
| `mVehicleName[64]` | char[64] | +32 | — | 車両名 |
| `mTrackName[64]` | char[64] | +96 | — | サーキット名 |

#### 位置・速度・姿勢

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mPos` | LMUVect3 | +160 | m | ワールド座標 |
| `mLocalVel` | LMUVect3 | +184 | m/s | 車両ローカル座標系での速度 |
| `mLocalAccel` | LMUVect3 | +208 | m/s² | 車両ローカル座標系での加速度 |
| `mOri[3]` | LMUVect3[3] | +232 | — | 姿勢行列（3×3回転行列の各行） |
| `mLocalRot` | LMUVect3 | +304 | rad/s | ローカル座標系での回転速度 |
| `mLocalRotAccel` | LMUVect3 | +328 | rad/s² | ローカル座標系での回転加速度 |

#### エンジン・トランスミッション

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mGear` | int32 | +352 | — | ギア（-1=リバース, 0=ニュートラル, 1–n=前進） |
| `mEngineRPM` | double | +356 | rpm | エンジン回転数 |
| `mEngineWaterTemp` | double | +364 | ℃ | エンジン冷却水温度 |
| `mEngineOilTemp` | double | +372 | ℃ | エンジンオイル温度 |
| `mClutchRPM` | double | +380 | rpm | クラッチ側 RPM |
| `mEngineMaxRPM` | double | +532 | rpm | レブリミット |
| `mEngineTorque` | double | +592 | N·m | 現在のエンジントルク（追加トルク込み） |
| `mMaxGears` | uint8 | +605 | — | 前進ギア数（最大） |

#### ドライバー入力・ステアリング

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mUnfilteredThrottle` | double | +388 | 0.0–1.0 | スロットル（フィルタなし） |
| `mUnfilteredBrake` | double | +396 | 0.0–1.0 | ブレーキ（フィルタなし） |
| `mUnfilteredSteering` | double | +404 | -1.0–1.0 | ステアリング（フィルタなし、左=-1） |
| `mUnfilteredClutch` | double | +412 | 0.0–1.0 | クラッチ（フィルタなし） |
| `mFilteredThrottle` | double | +420 | 0.0–1.0 | スロットル（フィルタあり） |
| `mFilteredBrake` | double | +428 | 0.0–1.0 | ブレーキ（フィルタあり） |
| `mFilteredSteering` | double | +436 | -1.0–1.0 | ステアリング（フィルタあり） |
| `mFilteredClutch` | double | +444 | 0.0–1.0 | クラッチ（フィルタあり） |
| `mSteeringShaftTorque` | double | +452 | N·m | ステアリングシャフトトルク |
| `mVisualSteeringWheelRange` | float | +660 | deg | 表示上のステアリングホイール回転角度 |
| `mPhysicalSteeringWheelRange` | float | +692 | deg | 物理ステアリングホイール回転角度 |

#### サスペンション・空力

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mFront3rdDeflection` | double | +460 | m | フロント第三スプリング圧縮量 |
| `mRear3rdDeflection` | double | +468 | m | リア第三スプリング圧縮量 |
| `mFrontWingHeight` | double | +476 | m | フロントウイング高さ |
| `mFrontRideHeight` | double | +484 | m | フロントライドハイト |
| `mRearRideHeight` | double | +492 | m | リアライドハイト |
| `mDrag` | double | +500 | — | 抗力 |
| `mFrontDownforce` | double | +508 | N | フロントダウンフォース |
| `mRearDownforce` | double | +516 | N | リアダウンフォース |

#### 燃料

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mFuel` | double | +524 | L | 現在の燃料残量 |
| `mFuelCapacity` | double | +608 | L | 燃料タンク容量 |

#### ダメージ

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mScheduledStops` | uint8 | +540 | — | 予定ピットストップ回数 |
| `mOverheating` | bool | +541 | — | オーバーヒートアイコン表示中 |
| `mDetached` | bool | +542 | — | ホイール以外のパーツが外れているか |
| `mHeadlights` | bool | +543 | — | ヘッドライト状態 |
| `mDentSeverity[8]` | uint8[8] | +544 | 0–2 | 車体8箇所の凹み深刻度（0=なし, 1=軽度, 2=重度） |
| `mLastImpactET` | double | +552 | s | 最後の衝突発生セッション時刻 |
| `mLastImpactMagnitude` | double | +560 | — | 最後の衝突の強度 |
| `mLastImpactPos` | LMUVect3 | +568 | m | 最後の衝突位置 |

#### セクター・ピット・タイヤコンパウンド

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mCurrentSector` | int32 | +600 | — | 現在のセクター（0ベース。符号ビットにピットレーン走行を格納。例: 第3セクターからピット進入で 0x80000002） |
| `mSpeedLimiter` | uint8 | +604 | — | スピードリミッター ON/OFF |
| `mFrontTireCompoundIndex` | uint8 | +606 | — | フロントタイヤコンパウンドのインデックス（ブランド内） |
| `mRearTireCompoundIndex` | uint8 | +607 | — | リアタイヤコンパウンドのインデックス（ブランド内） |
| `mFrontFlapActivated` | uint8 | +616 | — | フロントフラップ活性化状態 |
| `mRearFlapActivated` | uint8 | +617 | — | リアフラップ（DRS）活性化状態 |
| `mRearFlapLegalStatus` | uint8 | +618 | — | 0=不許可, 1=条件検出済みだが未許可, 2=許可 |
| `mIgnitionStarter` | uint8 | +619 | — | 0=オフ, 1=イグニッション, 2=イグニッション+スターター |
| `mFrontTireCompoundName[18]` | char[18] | +620 | — | フロントタイヤコンパウンド名 |
| `mRearTireCompoundName[18]` | char[18] | +638 | — | リアタイヤコンパウンド名 |
| `mSpeedLimiterAvailable` | uint8 | +656 | — | スピードリミッター装備の有無 |
| `mAntiStallActivated` | uint8 | +657 | — | アンチストール作動中 |
| `mUnused[2]` | uint8[2] | +658 | — | 未使用 |

#### ブレーキバイアス・ターボ・ハイブリッド

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mRearBrakeBias` | double | +664 | 0.0–1.0 | リアブレーキバイアス比率 |
| `mTurboBoostPressure` | double | +672 | — | ターボブースト圧（利用可能な場合） |
| `mPhysicsToGraphicsOffset[3]` | float[3] | +680 | m | 静的 CG からグラフィック中心へのオフセット |
| `mDeltaBest` | double | +696 | s | ベストラップに対するデルタタイム |
| `mBatteryChargeFraction` | double | +704 | 0.0–1.0 | バッテリー残量割合 |
| `mElectricBoostMotorTorque` | double | +712 | N·m | 電動ブーストモータートルク（回生時は負値） |
| `mElectricBoostMotorRPM` | double | +720 | rpm | 電動ブーストモーター回転数 |
| `mElectricBoostMotorTemperature` | double | +728 | ℃ | 電動ブーストモーター温度 |
| `mElectricBoostWaterTemperature` | double | +736 | ℃ | 電動ブーストモーター冷却水温度（なければ 0） |
| `mElectricBoostMotorState` | uint8 | +744 | enum | 0=使用不可, 1=非活性, 2=推進（放電）, 3=回生 |

#### LMU 固有フィールド（rF2 プラグイン版には存在しない）

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mLapInvalidated` | bool | +745 | — | 現在ラップが無効化されているか |
| `mABSActive` | bool | +746 | — | ABS 作動中 |
| `mTCActive` | bool | +747 | — | TC 作動中 |
| `mSpeedLimiterActive` | bool | +748 | — | スピードリミッター作動中 |
| `mWiperState` | uint8 | +749 | — | ワイパー（0=オフ, 1=自動, 2=低速, 3=高速） |
| `mTC` | uint8 | +750 | — | TC 設定値 |
| `mTCMax` | uint8 | +751 | — | TC 最大ステップ数 |
| `mTCSlip` | uint8 | +752 | — | TC スリップ設定値 |
| `mTCSlipMax` | uint8 | +753 | — | TC スリップ最大ステップ数 |
| `mTCCut` | uint8 | +754 | — | TC カット設定値 |
| `mTCCutMax` | uint8 | +755 | — | TC カット最大ステップ数 |
| `mABS` | uint8 | +756 | — | ABS 設定値 |
| `mABSMax` | uint8 | +757 | — | ABS 最大ステップ数 |
| `mMotorMap` | uint8 | +758 | — | モーターマップ設定値 |
| `mMotorMapMax` | uint8 | +759 | — | モーターマップ最大ステップ数 |
| `mMigration` | uint8 | +760 | — | ブレーキマイグレーション設定値 |
| `mMigrationMax` | uint8 | +761 | — | ブレーキマイグレーション最大ステップ数 |
| `mFrontAntiSway` | uint8 | +762 | — | フロントアンチロールバー設定値 |
| `mFrontAntiSwayMax` | uint8 | +763 | — | フロントアンチロールバー最大ステップ数 |
| `mRearAntiSway` | uint8 | +764 | — | リアアンチロールバー設定値 |
| `mRearAntiSwayMax` | uint8 | +765 | — | リアアンチロールバー最大ステップ数 |
| `mLiftAndCoastProgress` | uint8 | +766 | — | リフト＆コースト進行度 |
| `mTrackLimitsSteps` | uint8 | +767 | — | 正規化トラックリミットポイント（TrackLimitPoints × TrackLimitStepsPerPoint） |
| `mRegen` | float | +768 | kW | 回生量 |
| `mStateOfCharge` | float | +772 | % | バッテリー充電状態 |
| `mVirtualEnergy` | float | +776 | 0.0–1.0 | バーチャルエナジー残量割合 |
| `mTimeGapCarAhead` | float | +780 | s | 前方車両とのギャップ |
| `mTimeGapCarBehind` | float | +784 | s | 後方車両とのギャップ |
| `mTimeGapPlaceAhead` | float | +788 | s | 前順位車両とのギャップ |
| `mTimeGapPlaceBehind` | float | +792 | s | 後順位車両とのギャップ |
| `mVehicleModel[30]` | char[30] | +796 | — | ブランド＆モデル名 |
| `mVehicleClass` | uint8 | +826 | — | クラス識別値 |
| `mVehicleChampionship` | uint8 | +827 | — | 選手権＆年度識別値 |
| `mExpansion[20]` | uint8[20] | +828 | — | 将来拡張用 |
| `mWheels[4]` | LMUWheel[4] | +848 | — | ホイール情報（FL/FR/RL/RR、下表参照） |

---

## LMUWheel（TelemWheelV01、タイヤ・ホイール情報、260 bytes）

ホイール配列は `mWheels[4]`（FL=0, FR=1, RL=2, RR=3）の順。stride **260 bytes**、先頭は `vehicleBase + 848`。

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mSuspensionDeflection` | double | +0 | m | サスペンション圧縮量 |
| `mRideHeight` | double | +8 | m | ライドハイト |
| `mSuspForce` | double | +16 | N | プッシュロッド荷重 |
| `mBrakeTemp` | double | +24 | ℃ | ブレーキ温度 |
| `mBrakePressure` | double | +32 | 0.0–1.0 | ブレーキ圧（現状ドライバー入力とブレーキバランス依存。将来 kPa の実圧に変更予定） |
| `mRotation` | double | +40 | rad/s | ホイール回転速度 |
| `mLateralPatchVel` | double | +48 | m/s | 接地面横方向速度 |
| `mLongitudinalPatchVel` | double | +56 | m/s | 接地面縦方向速度 |
| `mLateralGroundVel` | double | +64 | m/s | 地面に対する横方向速度 |
| `mLongitudinalGroundVel` | double | +72 | m/s | 地面に対する縦方向速度 |
| `mCamber` | double | +80 | rad | キャンバー角 |
| `mLateralForce` | double | +88 | N | 横方向力 |
| `mLongitudinalForce` | double | +96 | N | 縦方向力 |
| `mTireLoad` | double | +104 | N | タイヤ垂直荷重 |
| `mGripFract` | double | +112 | 0.0–1.0 | スリップしている接地面の割合（近似値） |
| `mPressure` | double | +120 | kPa | タイヤ空気圧 |
| `mTemperature[3]` | double[3] | +128 (+136=中央, +144=右) | K | タイヤ表面温度（左/中央/右。内/中/外ではない点に注意） |
| `mWear` | double | +152 | 0.0–1.0 | タイヤ摩耗（0=新品, 1=完全摩耗。グリップ低下と比例するとは限らない） |
| `mTerrainName[16]` | char[16] | +160 | — | 路面種別名（TDFファイルのプレフィックス） |
| `mSurfaceType` | uint8 | +176 | enum | 路面タイプ（下表参照） |
| `mFlat` | bool | +177 | — | パンクしているか |
| `mDetached` | bool | +178 | — | ホイールが外れているか |
| `mStaticUndeflectedRadius` | uint8 | +179 | cm | タイヤ基本半径 |
| `mVerticalTireDeflection` | double | +180 | m | 速度依存半径からのタイヤ変形量 |
| `mWheelYLocation` | double | +188 | m | 車両相対 Y 位置 |
| `mToe` | double | +196 | rad | 現在のトー角 |
| `mTireCarcassTemperature` | double | +204 | K | タイヤカーカス平均温度 |
| `mTireInnerLayerTemperature[3]` | double[3] | +212 | K | タイヤ内層温度（左/中央/右） |
| `mOptimalTemp` | float | +236 | ℃ | 最適温度 |
| `mCompoundIndex` | uint8 | +240 | — | 車両・トラック別の利用可能コンパウンドリスト内インデックス |
| `mCompoundType` | uint8 | +241 | enum | 0=ソフト, 1=ミディアム, 2=ハード, 3=ウェット |
| `mExpansion[18]` | uint8[18] | +242 | — | 将来拡張用 |

### mSurfaceType 列挙値

| 値 | 意味 |
|---|---|
| 0 | Dry（乾燥路） |
| 1 | Wet（濡れた路面） |
| 2 | Grass（草） |
| 3 | Dirt（土） |
| 4 | Gravel（砂利） |
| 5 | Rumblestrip（縁石） |
| 6 | Special（特殊） |

---

## 注意事項

- **`mBestSector2` / `mLastSector2` / `mCurSector2` は S1+S2 の累積値**。S2 単体の時間は `mBestSector2 - mBestSector1` のように差分で計算する
- **タイヤ温度（表面・カーカス・内層）は Kelvin**（摂氏変換: K − 273.15）。ブレーキ温度・水温・油温は摂氏
- **`mSector`（0=S3, 1=S1, 2=S2）** と直感に反する順序に注意
- **`_pack_=4` レイアウト**：`LMU_Data` は 4 バイト境界でアライメントされる。double が 8 バイト境界に乗らない箇所があるため、オフセットは本ドキュメントの実測値を使うこと
- **タイム系フィールドは無効時に負値**（例: ラップ未計測時の `mBestLapTime` は -1.0）になるため、正値のみを有効値として扱う
- ポインタ格納用フィールド（`mResultsStreamPointer`, `mVehiclePointer`）はゲームプロセス内部のアドレスであり、共有メモリの読み手には意味がない

---

## KoDriver での実装済みフィールド一覧

`LmuWindowsMapper` および各 Repository 実装（`LmuWindowsFlagRepositoryImpl`, `LmuWindowsVehicleDamageRepositoryImpl`, `LmuWindowsVehicleApproachRepositoryImpl`）が読み取っているフィールド。`vehicleBase = 128464 + 4 + playerVehicleIdx × 1888`、`wheelBase = vehicleBase + 848 + wheelIdx × 260`、`scoringVehicleBase = 2192 + n × 584`（`mIsPlayer` で線形探索）。

| 機能 | フィールド | オフセット |
|---|---|---|
| エンジン RPM | `mEngineRPM` | vehicleBase+356 |
| レブリミット | `mEngineMaxRPM` | vehicleBase+532 |
| ギア | `mGear` | vehicleBase+352 |
| スロットル | `mUnfilteredThrottle` | vehicleBase+388 |
| ブレーキ | `mUnfilteredBrake` | vehicleBase+396 |
| ステアリング | `mUnfilteredSteering` | vehicleBase+404 |
| クラッチ | `mUnfilteredClutch` | vehicleBase+412 |
| 燃料残量 | `mFuel` | vehicleBase+524 |
| 燃料タンク容量 | `mFuelCapacity` | vehicleBase+608 |
| 現在ラップ | `mLapNumber` | vehicleBase+20 |
| 最大ラップ | `mMaxLaps` | 1632+84 |
| セッション経過時間 | `mCurrentET` | 1632+68 |
| 車両数 | `mNumVehicles` | 1632+104 |
| 位置 X/Y/Z | `mPos` | vehicleBase+160/168/176 |
| ローカル速度 X/Y/Z | `mLocalVel` | vehicleBase+184/192/200 |
| ベストラップタイム | `mBestLapTime` | scoringVehicleBase+144 |
| 前周ラップタイム | `mLastLapTime` | scoringVehicleBase+168 |
| ラップ開始時刻 | `mLapStartET` | scoringVehicleBase+256 |
| ベストラップ S1/S2 | `mBestLapSector1`/`mBestLapSector2` | scoringVehicleBase+576/580 |
| プレイヤー判定 | `mIsPlayer` | scoringVehicleBase+196 |
| ブレーキ温度 | `mBrakeTemp` | wheelBase+24 |
| タイヤ空気圧 | `mPressure` | wheelBase+120 |
| タイヤ表面温度（中央） | `mTemperature[1]` | wheelBase+136 |
| タイヤ摩耗 | `mWear` | wheelBase+152 |
| タイヤカーカス温度 | `mTireCarcassTemperature` | wheelBase+204 |
| ゲームフェーズ | `mGamePhase` | 1632+108 |
| イエローフラッグ状態 | `mYellowFlagState` | 1632+109 |
| セクターフラッグ | `mSectorFlag[3]` | 1632+110 |
| スタートライト / 赤ライト数 | `mStartLight` / `mNumRedLights` | 1632+113 / 1632+114 |
| 提示フラッグ | `mFlag` | scoringVehicleBase+504 |
| FCY 下判定 | `mUnderYellow` | scoringVehicleBase+505 |
| ラップカウント種別 | `mCountLapFlag` | scoringVehicleBase+506 |
| オーバーヒート | `mOverheating` | vehicleBase+541 |
| パーツ脱落 | `mDetached` | vehicleBase+542 |
| 最終衝突強度 | `mLastImpactMagnitude` | vehicleBase+560 |
| 姿勢行列（第2行 X/Z） | `mOri[2]` | vehicleBase+280/296 |

---

## 参考リポジトリ・情報源

| リポジトリ | 言語 | 概要 |
|---|---|---|
| ゲーム内 `Support\SharedMemoryInterface` | C++ | S397 純正ヘッダ。`LMU_Data` の構造体定義の正典 |
| [TinyPedal/pyLMUSharedMemory](https://github.com/TinyPedal/pyLMUSharedMemory) | Python | 純正ヘッダの ctypes 移植（`lmu_data.py`）。本ドキュメントのオフセット算出元 |
| [pyLMUSharedMemory `lmu_enum.py`](https://github.com/TinyPedal/pyLMUSharedMemory/blob/master/lmu_enum.py) | Python | 各 enum フィールドの値マッピング。LMU API 1.3 spec 対応（2026年3月）で追加された |
| [pyLMUSharedMemory `tests/read_lmu_api.py`](https://github.com/TinyPedal/pyLMUSharedMemory/blob/master/tests/read_lmu_api.py) | Python | 構造体サイズの照合（`compare_struct_size`）と全フィールドのダンプを行う検証スクリプト。Windows 実機でレイアウトの一致を確認する際に使う |
| [TinyPedal/TinyPedal](https://github.com/TinyPedal/TinyPedal) | Python | 最も活発な LMU 対応オーバーレイ。フィールド利用例が豊富 |
| [TheIronWolfModding/rF2SharedMemoryMapPlugin](https://github.com/TheIronWolfModding/rF2SharedMemoryMapPlugin) | C++/C# | rFactor 2 用共有メモリプラグイン。`LMU_Data` とは別物だが構造体の系譜が共通 |
