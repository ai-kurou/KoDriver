# rFactor2 / Le Mans Ultimate 共有メモリ API 完全フィールドリファレンス

LMU は rFactor2 エンジンベースのため、共有メモリ API は rF2SharedMemoryMapPlugin と互換性がある。

---

## 共有メモリセグメント一覧

| セグメント名 | 説明 | LMU でのセグメント名 |
|---|---|---|
| `$rFactor2SMMP_Telemetry$` | 車両テレメトリ | `LMU_Data` |
| `$rFactor2SMMP_Scoring$` | スコアリング/レース状態 | 同上 |
| `$rFactor2SMMP_Rules$` | トラックルール | 同上 |
| `$rFactor2SMMP_MultiRules$` | マルチセッションルール | 同上 |
| `$rFactor2SMMP_ForceFeedback$` | フォースフィードバック | 同上 |
| `$rFactor2SMMP_Graphics$` | グラフィクス情報 | 同上 |
| `$rFactor2SMMP_PitInfo$` | ピットメニュー情報 | 同上 |
| `$rFactor2SMMP_Weather$` | 天候情報 | 同上 |
| `$rFactor2SMMP_Extended$` | Studio 397 拡張 | 同上 |

LMU の場合、単一セグメント `LMU_Data`（約324,820バイト）から全データを読み取る。

---

## LMUObjectOut レイアウト（KoDriver の実装より）

| セグメント | 先頭オフセット | サイズ |
|---|---|---|
| `LMUGeneric`（汎用情報） | 0 | 332 bytes |
| `LMUPathData`（パス情報） | 332 | 1300 bytes |
| `LMUScoringData`（スコアリング） | 1632 | 126,832 bytes |
| `LMUTelemetryData`（テレメトリ） | 128,464 | 196,356 bytes |

---

## 基本型

### rF2Vec3

| フィールド | 型 | 説明 |
|---|---|---|
| `x` | double | X 軸成分 |
| `y` | double | Y 軸成分（上方向） |
| `z` | double | Z 軸成分 |

---

## rF2Wheel（タイヤ・ホイール情報）

ホイール配列は `mWheels[4]`（FL=0, FR=1, RL=2, RR=3）の順。
KoDriver での stride は **260 bytes**、先頭オフセット `vehicleBase + 848`。

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mSuspensionDeflection` | double | +0 | m | サスペンション圧縮量 |
| `mRideHeight` | double | +8 | m | ライドハイト |
| `mSuspForce` | double | +16 | N | プッシュロード荷重 |
| `mBrakeTemp` | double | +24 | ℃ | ブレーキ温度 |
| `mBrakePressure` | double | +32 | 0.0–1.0 | ブレーキ圧（ドライバー入力ベース） |
| `mRotation` | double | +40 | rad/s | ホイール回転速度 |
| `mLateralPatchVel` | double | +48 | m/s | 接地面横方向速度 |
| `mLongitudinalPatchVel` | double | +56 | m/s | 接地面縦方向速度 |
| `mLateralGroundVel` | double | +64 | m/s | 地面に対する横方向速度 |
| `mLongitudinalGroundVel` | double | +72 | m/s | 地面に対する縦方向速度 |
| `mCamber` | double | +80 | rad | キャンバー角 |
| `mLateralForce` | double | +88 | N | 横方向力 |
| `mLongitudinalForce` | double | +96 | N | 縦方向力 |
| `mTireLoad` | double | +104 | N | タイヤ垂直荷重 |
| `mGripFract` | double | +112 | 0.0–1.0 | スリップしている接地面の割合 |
| `mPressure` | double | +120 | kPa | タイヤ空気圧 |
| `mTemperature[3]` | double[3] | +128 (+8=中央, +16=右) | K | タイヤ表面温度（左/中央/右） |
| `mWear` | double | +152 | 0.0–1.0 | タイヤ摩耗（0=新品, 1=完全摩耗） |
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
| `mExpansion[24]` | uint8[24] | — | — | 将来拡張用 |

### SurfaceType 列挙値

| 値 | 意味 |
|---|---|
| 0 | Dry（乾燥路） |
| 1 | Wet（濡れた路面） |
| 2 | Grass（草） |
| 3 | Dirt（土） |
| 4 | Gravel（砂利） |
| 5 | Kerb（縁石） |
| 6 | Special（特殊） |

---

## rF2VehicleTelemetry（車両テレメトリ）

`rF2Telemetry.mVehicles[128]` の各要素。LMU では stride **1888 bytes**、先頭オフセット `128464 + 4 + playerIdx * 1888`。

### 識別・時間

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mID` | int32 | +0 | — | スロット ID |
| `mDeltaTime` | double | +8 | s | 前回更新からの経過時間 |
| `mElapsedTime` | double | +16 | s | セッション開始からの経過時間 |
| `mLapNumber` | int32 | +20 | — | 現在のラップ番号 |
| `mLapStartET` | double | +24 | s | 現在ラップ開始時のセッション時間 |
| `mVehicleName[64]` | char[64] | +32 | — | 車両名 |
| `mTrackName[64]` | char[64] | +96 | — | サーキット名 |

### 位置・速度・姿勢

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mPos` | rF2Vec3 | +160 | m | ワールド座標 |
| `mLocalVel` | rF2Vec3 | +184 | m/s | 車両ローカル座標系での速度 |
| `mLocalAccel` | rF2Vec3 | +208 | m/s² | 車両ローカル座標系での加速度 |
| `mOri[3]` | rF2Vec3[3] | +232 | — | 姿勢行列（3×3回転行列の各行） |
| `mLocalRot` | rF2Vec3 | +304 | rad/s | ローカル座標系での回転速度 |
| `mLocalRotAccel` | rF2Vec3 | +328 | rad/s² | ローカル座標系での回転加速度 |

### エンジン・トランスミッション

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mGear` | int32 | +352 | — | ギア（-1=リバース, 0=ニュートラル, 1–n=前進） |
| `mEngineRPM` | double | +356 | rpm | エンジン回転数 |
| `mEngineWaterTemp` | double | +364 | ℃ | エンジン冷却水温度 |
| `mEngineOilTemp` | double | +372 | ℃ | エンジンオイル温度 |
| `mClutchRPM` | double | +380 | rpm | クラッチ側 RPM |
| `mEngineTorque` | double | +548 | N·m | 現在のエンジントルク |
| `mEngineMaxRPM` | double | +532 | rpm | レブリミット |
| `mMaxGears` | uint8 | +577 | — | 前進ギア数（最大） |

### ドライバー入力

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
| `mVisualSteeringWheelRange` | float | — | deg | 表示上のステアリングホイール回転角度 |
| `mPhysicalSteeringWheelRange` | float | — | deg | 物理ステアリングホイール回転角度 |

### サスペンション・空力

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mFront3rdDeflection` | double | m | フロント第三スプリング圧縮量 |
| `mRear3rdDeflection` | double | m | リア第三スプリング圧縮量 |
| `mFrontWingHeight` | double | m | フロントウイング高さ |
| `mFrontRideHeight` | double | m | フロントライドハイト |
| `mRearRideHeight` | double | m | リアライドハイト |
| `mDrag` | double | — | 抗力係数 |
| `mFrontDownforce` | double | N | フロントダウンフォース |
| `mRearDownforce` | double | N | リアダウンフォース |

### 燃料

| フィールド | 型 | オフセット（相対） | 単位 | 説明 |
|---|---|---|---|---|
| `mFuel` | double | +524 | L | 現在の燃料残量 |
| `mFuelCapacity` | double | +608 | L | 燃料タンク容量 |

### タイヤコンパウンド

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mFrontTireCompoundIndex` | uint8 | — | フロントタイヤコンパウンドのインデックス |
| `mRearTireCompoundIndex` | uint8 | — | リアタイヤコンパウンドのインデックス |
| `mFrontTireCompoundName[18]` | char[18] | — | フロントタイヤコンパウンド名 |
| `mRearTireCompoundName[18]` | char[18] | — | リアタイヤコンパウンド名 |

### ダメージ

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mDentSeverity[8]` | uint8[8] | 0–255 | 車体8箇所の凹み深刻度（前/後/左/右/他） |
| `mLastImpactET` | double | s | 最後の衝突発生セッション時刻 |
| `mLastImpactMagnitude` | double | — | 最後の衝突の強度 |
| `mLastImpactPos` | rF2Vec3 | m | 最後の衝突位置 |
| `mOverheating` | bool | — | オーバーヒートアイコン表示中 |
| `mDetached` | bool | — | ホイール以外のパーツが外れているか |

### ブレーキバイアス・ターボ・ERS

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mRearBrakeBias` | double | 0.0–1.0 | リアブレーキバイアス比率 |
| `mTurboBoostPressure` | double | — | ターボブースト圧（利用可能な場合） |
| `mBatteryChargeFraction` | double | 0.0–1.0 | バッテリー残量 |
| `mElectricBoostMotorTorque` | double | N·m | 電動ブーストモータートルク |
| `mElectricBoostMotorRPM` | double | rpm | 電動ブーストモーター回転数 |
| `mElectricBoostMotorTemperature` | double | K | 電動ブーストモーター温度 |
| `mElectricBoostWaterTemperature` | double | K | 電動ブーストモーター冷却水温度 |
| `mElectricBoostMotorState` | uint8 | enum | 0=使用不可, 1=非活性, 2=充電中, 3=放電中など |

### ピット・スピードリミッター・その他

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mScheduledStops` | uint8 | — | 予定ピットストップ回数 |
| `mSpeedLimiter` | uint8 | — | スピードリミッター ON/OFF |
| `mSpeedLimiterAvailable` | uint8 | — | スピードリミッター装備の有無 |
| `mFrontFlapActivated` | uint8 | — | フロントフラップ（DRS）活性化状態 |
| `mRearFlapActivated` | uint8 | — | リアフラップ（DRS）活性化状態 |
| `mRearFlapLegalStatus` | uint8 | — | 0=不許可, 1=検出済み, 2=許可 |
| `mAntiStallActivated` | uint8 | — | アンチストール作動中 |
| `mIgnitionStarter` | uint8 | — | 0=オフ, 1=イグニッション, 2=イグニッション+スターター |
| `mCurrentSector` | int32 | — | 現在のセクター（0ベース） |
| `mHeadlights` | bool | — | ヘッドライト状態 |
| `mPhysicsToGraphicsOffset[3]` | float[3] | m | 静的 CG からのオフセット |
| `mWheels[4]` | rF2Wheel[4] | — | ホイール情報（FL/FR/RL/RR） |

---

## rF2ScoringInfo（レース全体情報）

LMU では `SCORING_BASE = 1632` から始まる。

| フィールド | 型 | オフセット（絶対） | 単位 | 説明 |
|---|---|---|---|---|
| `mTrackName[64]` | char[64] | 1632+0 | — | サーキット名 |
| `mSession` | int32 | 1632+64 | enum | セッション種別（下表参照） |
| `mCurrentET` | double | 1632+68 | s | 現在のセッション経過時間 |
| `mEndET` | double | 1632+76 | s | セッション終了時刻 |
| `mMaxLaps` | int32 | 1632+84 | — | 最大ラップ数 |
| `mLapDist` | double | 1632+88 | m | サーキット1周の距離 |
| `mNumVehicles` | int32 | — | — | 現在の車両数 |
| `mGamePhase` | uint8 | — | enum | ゲームフェーズ（下表参照） |
| `mYellowFlagState` | int8 | — | enum | イエローフラッグ状態（下表参照） |
| `mSectorFlag[3]` | int8[3] | — | — | セクターごとのローカルイエロー |
| `mStartLight` | uint8 | — | — | スタートライト（フレーム） |
| `mNumRedLights` | uint8 | — | — | スタートシーケンスの赤ライト数 |
| `mInRealtime` | bool | — | — | リアルタイム（走行中）か否か |
| `mPlayerName[32]` | char[32] | — | — | プレイヤー名 |
| `mPlrFileName[64]` | char[64] | — | — | プレイヤーファイル名 |
| `mDarkCloud` | double | — | 0.0–1.0 | 雲の暗さ |
| `mRaining` | double | — | 0.0–1.0 | 降雨強度 |
| `mAmbientTemp` | double | — | ℃ | 大気温度 |
| `mTrackTemp` | double | — | ℃ | 路面温度 |
| `mWind` | rF2Vec3 | — | m/s | 風速ベクトル |
| `mMinPathWetness` | double | — | 0.0–1.0 | 最小路面湿度 |
| `mMaxPathWetness` | double | — | 0.0–1.0 | 最大路面湿度 |
| `mAvgPathWetness` | double | — | 0.0–1.0 | 平均路面湿度 |
| `mGameMode` | uint8 | — | — | 1=サーバー, 2=クライアント, 3=両方 |
| `mIsPasswordProtected` | bool | — | — | サーバーパスワード保護 |
| `mServerPort` | uint16 | — | — | サーバーポート番号 |
| `mServerPublicIP` | uint32 | — | — | サーバーパブリック IP |
| `mMaxPlayers` | int32 | — | — | セッション最大車両数 |
| `mServerName[32]` | char[32] | — | — | サーバー名 |
| `mStartET` | float | — | s（深夜0時からの秒数） | セッション開始時刻 |

### mSession 列挙値

| 値 | 意味 |
|---|---|
| 0 | Test Day（テスト走行） |
| 1–4 | Practice（練習走行） |
| 5–8 | Qualifying（予選） |
| 9–10 | Warmup（ウォームアップ） |
| 11–14 | Race（決勝） |

### mGamePhase 列挙値

| 値 | 意味 |
|---|---|
| 0 | Garage（ガレージ） |
| 1 | WarmUp（ウォームアップラップ） |
| 2 | GridWalk（グリッドウォーク） |
| 3 | Formation（フォーメーションラップ） |
| 4 | Countdown（カウントダウン） |
| 5 | GreenFlag（レース中） |
| 6 | FullCourseYellow（FCY） |
| 7 | SessionStopped（セッション停止） |
| 8 | SessionOver（セッション終了） |

### mYellowFlagState 列挙値

| 値 | 意味 |
|---|---|
| -1 | Invalid |
| 0 | NoFlag（グリーン） |
| 1 | Pending（FCY 発動保留） |
| 2 | PitClosed（ピットクローズ） |
| 3 | PitLeadLap（先頭周回のみピット可） |
| 4 | PitOpen（ピットオープン） |
| 5 | LastLap（最終周） |
| 6 | Resume（リスタート） |
| 7 | RaceHalt（レース中断） |

---

## rF2VehicleScoring（各車両のレース状態）

`rF2Scoring.mVehicles[128]` の各要素。

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mID` | int32 | — | スロット ID |
| `mDriverName[32]` | char[32] | — | ドライバー名 |
| `mVehicleName[64]` | char[64] | — | 車両名 |
| `mTotalLaps` | int16 | — | 完了ラップ数 |
| `mSector` | int8 | — | 現在のセクター（**0=S3, 1=S1, 2=S2**） |
| `mFinishStatus` | int8 | — | 0=未完走, 1=完走, 2=DNF, 3=DQ |
| `mLapDist` | double | m | サーキット上の現在位置 |
| `mPathLateral` | double | m | センターラインからの横方向距離 |
| `mTrackEdge` | double | m | 同じ側のトラックエッジまでの距離 |
| `mBestSector1` | double | s | ベストセクター1タイム |
| `mBestSector2` | double | s | ベストセクター2累積タイム（S1+S2） |
| `mBestLapTime` | double | s | ベストラップタイム |
| `mLastSector1` | double | s | 前周のセクター1タイム |
| `mLastSector2` | double | s | 前周のセクター2累積タイム |
| `mLastLapTime` | double | s | 前周のラップタイム |
| `mCurSector1` | double | s | 現在周のセクター1タイム（有効な場合） |
| `mCurSector2` | double | s | 現在周のセクター2累積タイム（有効な場合） |
| `mNumPitstops` | int16 | — | ピットストップ回数 |
| `mNumPenalties` | int16 | — | 未消化ペナルティ数 |
| `mIsPlayer` | bool | — | プレイヤーの車両か |
| `mControl` | int8 | — | -1=なし, 0=プレイヤー, 1=AI, 2=リモート, 3=リプレイ |
| `mInPits` | bool | — | ピットレーン走行中 |
| `mPlace` | uint8 | — | 順位（1ベース） |
| `mVehicleClass[32]` | char[32] | — | クラス名 |
| `mTimeBehindNext` | double | s | 次の車両との時間差 |
| `mLapsBehindNext` | int32 | — | 次の車両との周回差 |
| `mTimeBehindLeader` | double | s | トップとの時間差 |
| `mLapsBehindLeader` | int32 | — | トップとの周回差 |
| `mLapStartET` | double | s | 現在ラップ開始セッション時刻 |
| `mPos` | rF2Vec3 | m | ワールド座標 |
| `mLocalVel` | rF2Vec3 | m/s | ローカル速度 |
| `mLocalAccel` | rF2Vec3 | m/s² | ローカル加速度 |
| `mOri[3]` | rF2Vec3[3] | — | 姿勢行列 |
| `mLocalRot` | rF2Vec3 | rad/s | ローカル回転速度 |
| `mLocalRotAccel` | rF2Vec3 | rad/s² | ローカル回転加速度 |
| `mHeadlights` | uint8 | — | ヘッドライト状態 |
| `mPitState` | uint8 | — | 0=なし, 1=要求, 2=入場中, 3=サービス中, 4=出場中 |
| `mServerScored` | uint8 | — | サーバーによってスコアリングされているか |
| `mIndividualPhase` | uint8 | — | 個別のゲームフェーズ（0–8 + 9–11の拡張値） |
| `mQualification` | int32 | — | 予選順位（1ベース、無効時は-1） |
| `mTimeIntoLap` | double | s | 現在ラップ内の推定経過時間 |
| `mEstimatedLapTime` | double | s | 推定ラップタイム |
| `mPitGroup[24]` | char[24] | — | ピットグループ名 |
| `mFlag` | uint8 | — | 0=グリーン, 6=ブルー（この車両への提示フラッグ） |
| `mUnderYellow` | bool | — | フルコースイエロー状況下にあるか |
| `mCountLapFlag` | uint8 | — | 0=カウントなし, 1=ラップのみ, 2=ラップ+タイム |
| `mInGarageStall` | bool | — | 正しいガレージストール内にいるか |
| `mUpgradePack[16]` | uint8[16] | — | アップグレードパック情報（エンコード済み） |
| `mPitLapDist` | float | m | ピットロケーションのラップ距離位置 |
| `mBestLapSector1` | float | s | ベストラップ時のセクター1タイム |
| `mBestLapSector2` | float | s | ベストラップ時のセクター2タイム |

---

## rF2PhysicsOptions（物理オプション）

`rF2Extended.mPhysics` に格納。

| フィールド | 型 | 値範囲 | 説明 |
|---|---|---|---|
| `mTractionControl` | uint8 | 0–3 | トラクションコントロール強度 |
| `mAntiLockBrakes` | uint8 | 0–2 | ABS 強度 |
| `mStabilityControl` | uint8 | 0–2 | スタビリティコントロール強度 |
| `mAutoShift` | uint8 | 0–3 | 0=オフ, 1=アップのみ, 2=ダウンのみ, 3=全自動 |
| `mAutoClutch` | uint8 | 0–1 | オートクラッチ |
| `mInvulnerable` | uint8 | 0–1 | 無敵モード |
| `mOppositeLock` | uint8 | 0–1 | カウンターステア補助 |
| `mSteeringHelp` | uint8 | 0–3 | ステアリング補助強度 |
| `mBrakingHelp` | uint8 | 0–2 | ブレーキ補助強度 |
| `mSpinRecovery` | uint8 | 0–1 | スピン回復補助 |
| `mAutoPit` | uint8 | 0–1 | ピット自動化 |
| `mAutoLift` | uint8 | 0–1 | 自動リフト |
| `mAutoBlip` | uint8 | 0–1 | 自動ヒール＆トゥ |
| `mFuelMult` | uint8 | 0–7 | 燃料消費倍率 |
| `mTireMult` | uint8 | 0–7 | タイヤ摩耗倍率 |
| `mMechFail` | uint8 | 0–2 | 0=オフ, 1=通常, 2=タイムスケール |
| `mAllowPitcrewPush` | uint8 | 0–1 | ピットクルーによるプッシュ許可 |
| `mRepeatShifts` | uint8 | 0–5 | 誤シフト防止レベル |
| `mHoldClutch` | uint8 | 0–1 | クラッチ保持 |
| `mAutoReverse` | uint8 | 0–1 | 自動リバース |
| `mAlternateNeutral` | uint8 | 0–1 | 上下同時入力でニュートラル |
| `mAIControl` | uint8 | 0–1 | AI によるプレイヤー操作 |
| `mManualShiftOverrideTime` | float | s | 手動シフト後の自動シフト復帰時間 |
| `mAutoShiftOverrideTime` | float | s | 自動シフト後の手動シフト復帰時間 |
| `mSpeedSensitiveSteering` | float | 0.0–1.0 | 速度感応ステアリング |
| `mSteerRatioSpeed` | float | m/s | ロック拡張用の基準速度 |

---

## rF2GraphicsInfo（グラフィクス情報）

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mCamPos` | rF2Vec3 | m | カメラ位置 |
| `mCamOri[3]` | rF2Vec3[3] | — | カメラ姿勢行列 |
| `mHWND` | HWND | — | アプリウィンドウハンドル |
| `mAmbientRed` | double | 0.0–1.0 | 環境光 R 成分 |
| `mAmbientGreen` | double | 0.0–1.0 | 環境光 G 成分 |
| `mAmbientBlue` | double | 0.0–1.0 | 環境光 B 成分 |
| `mID` | int32 | — | 現在視点の車両スロット ID（-1=無効） |
| `mCameraType` | int32 | — | カメラタイプ（TVコックピット/コックピット/ノーズカム等） |

---

## rF2ForceFeedback（フォースフィードバック）

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mForceValue` | double | -1.0–1.0 | 現在の FFB 出力値 |

---

## rF2PitMenu（ピットメニュー）

| フィールド | 型 | 説明 |
|---|---|---|
| `mCategoryIndex` | int32 | 現在のカテゴリーインデックス |
| `mCategoryName[32]` | char[32] | カテゴリー名（未翻訳） |
| `mChoiceIndex` | int32 | 現在の選択肢インデックス |
| `mChoiceString[32]` | char[32] | 選択肢文字列（一部翻訳済み） |
| `mNumChoices` | int32 | 選択肢の総数 |

---

## rF2WeatherControlInfo（天候情報）

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mET` | double | s | 天候変化が有効になるセッション時刻 |
| `mRaining[3][3]` | double[3][3] | 0.0–1.0 | 各ノード（左/中央/右）×3地点の降雨強度 |
| `mCloudiness` | double | 0.0–1.0 | 雲量 |
| `mAmbientTempK` | double | K | 大気温度 |
| `mWindMaxSpeed` | double | m/s | 最大風速 |
| `mApplyCloudinessInstantly` | bool | — | 即時変化か段階的変化か |

---

## rF2TrackRules（トラックルール・FCY）

| フィールド | 型 | 単位 | 説明 |
|---|---|---|---|
| `mCurrentET` | double | s | 現在の経過時間 |
| `mStage` | enum | — | ステージ（下表参照） |
| `mPoleColumn` | enum | — | ポールポジションの列 |
| `mNumActions` | int32 | — | 直近のアクション数 |
| `mNumParticipants` | int32 | — | 参加者数 |
| `mYellowFlagDetected` | bool | — | イエローフラッグ検出/要求済み |
| `mYellowFlagLapsWasOverridden` | uint8 | — | 管理者による上書き |
| `mSafetyCarExists` | bool | — | セーフティカーの存在 |
| `mSafetyCarActive` | bool | — | セーフティカー走行中 |
| `mSafetyCarLaps` | int32 | — | セーフティカー走行ラップ数 |
| `mSafetyCarThreshold` | float | — | セーフティカー投入閾値 |
| `mSafetyCarLapDist` | double | m | セーフティカーのラップ距離位置 |
| `mSafetyCarLapDistAtStart` | float | m | セーフティカー投入時の先頭位置 |
| `mPitLaneStartDist` | float | m | ピット入口のラップ距離位置 |
| `mTeleportLapDist` | float | m | テレポート位置（先頭から） |
| `mYellowFlagState` | int8 | enum | イエローフラッグ状態 |
| `mYellowFlagLaps` | int16 | — | 推奨イエローフラッグ周回数 |
| `mSafetyCarInstruction` | int32 | — | セーフティカー命令 |
| `mSafetyCarSpeed` | float | m/s | セーフティカー最大速度 |
| `mSafetyCarMinimumSpacing` | float | m | セーフティカー後方最小車間距離 |
| `mSafetyCarMaximumSpacing` | float | m | セーフティカー後方最大車間距離 |
| `mMinimumColumnSpacing` | float | m | 列内最小車間距離 |
| `mMaximumColumnSpacing` | float | m | 列内最大車間距離 |
| `mMinimumSpeed` | float | m/s | 最小速度制限 |
| `mMaximumSpeed` | float | m/s | 最大速度制限 |
| `mMessage[96]` | char[96] | — | ブロードキャストメッセージ（未翻訳） |

### TrackRulesStage 列挙値

| 値 | 意味 |
|---|---|
| 0 | FormationInit（フォーメーション初期化） |
| 1 | FormationUpdate（フォーメーション更新） |
| 2 | Normal（通常レース） |
| 3 | CautionInit（コーションフラッグ初期化） |
| 4 | CautionUpdate（コーションフラッグ更新） |

### TrackRulesColumn 列挙値

| 値 | 意味 |
|---|---|
| 0 | LeftLane |
| 1 | MidLeftLane |
| 2 | MiddleLane |
| 3 | MidRightLane |
| 4 | RightLane |
| 5 | Invalid |
| 6 | FreeChoice |
| 7 | Pending |

---

## rF2TrackRulesParticipant（FCY 参加者情報）

| フィールド | 型 | 説明 |
|---|---|---|
| `mID` | int32 | スロット ID |
| `mFrozenOrder` | int16 | コーション発動時の0ベース順位 |
| `mPlace` | int16 | 1ベース順位 |
| `mYellowSeverity` | float | その車両のイエロー貢献度 |
| `mCurrentRelativeDistance` | double | 相対距離計算値 |
| `mRelativeLaps` | int32 | フォーメーション/コーション周回差 |
| `mColumnAssignment` | enum | 列の割り当て |
| `mPositionAssignment` | int32 | 列内の0ベース位置 |
| `mPitsOpen` | uint8 | この車両にピットオープンか |
| `mUpToSpeed` | bool | フローズンオーダーで規定速度に達しているか |
| `mGoalRelativeDistance` | double | リーダー位置ベースの目標相対距離 |
| `mMessage[96]` | char[96] | 参加者メッセージ（未翻訳） |

---

## rF2TrackRulesAction（FCY アクション）

| フィールド | 型 | 説明 |
|---|---|---|
| `mCommand` | enum | 推奨アクション（下表参照） |
| `mID` | int32 | 対象スロット ID |
| `mET` | double | アクション発生時刻 |

### TrackRulesCommand 列挙値

| 値 | 意味 |
|---|---|
| 0 | AddFromTrack（コース上から追加） |
| 1 | AddFromPit（ピットから追加） |
| 2 | AddFromUndq（DQ解除から追加） |
| 3 | RemoveToPit（ピットへ退避） |
| 4 | RemoveToDnf（DNF） |
| 5 | RemoveToDq（失格） |
| 6 | RemoveToUnloaded（アンロード） |
| 7 | MoveToBack（最後尾へ） |
| 8 | LongestTime（最長時間） |

---

## rF2MultiSessionRules（マルチセッションルール）

| フィールド | 型 | 説明 |
|---|---|---|
| `mSession` | int32 | 現在のセッション番号 |
| `mSpecialSlotID` | int32 | 特別スロット ID またはリクエストフラッグ |
| `mTrackType[32]` | char[32] | GDB ファイルのトラックタイプ |
| `mNumParticipants` | int32 | 参加者数 |
| `mNumQualSessions` | int32 | 予選セッション数 |
| `mNumRaceSessions` | int32 | 決勝セッション数 |
| `mMaxLaps` | int32 | 最大ラップ数 |
| `mMaxSeconds` | int32 | 最大セッション時間（秒） |
| `mName[32]` | char[32] | セッション名上書き（未翻訳） |

---

## rF2MultiSessionParticipant（マルチセッション参加者）

| フィールド | 型 | 説明 |
|---|---|---|
| `mID` | int32 | スロット ID（切断時は-1） |
| `mDriverName[32]` | char[32] | ドライバー名 |
| `mVehicleName[64]` | char[64] | 車両名 |
| `mUpgradePack[16]` | uint8[16] | アップグレードパック情報 |
| `mBestPracticeTime` | float | s | ベスト練習タイム |
| `mQualParticipantIndex` | int32 | — | 予選参加者ランキング |
| `mQualificationTime[4]` | float[4] | s | 予選セッションごとのベストタイム |
| `mFinalRacePlace[4]` | float[4] | — | 決勝セッションごとの最終順位 |
| `mFinalRaceTime[4]` | float[4] | s | 決勝セッションごとの最終タイム |
| `mServerScored` | bool | — | 参加許可フラッグ |
| `mGridPosition` | int32 | — | 1ベースのグリッド位置（-1=無効） |

---

## rF2Extended（Studio 397 拡張バッファ）

| フィールド | 型 | 説明 |
|---|---|---|
| `mVersion[12]` | char[12] | API バージョン文字列 |
| `is64bit` | bool | 64bit プラグインか |
| `mPhysics` | rF2PhysicsOptions | 物理オプション |
| `mTrackedDamages[512]` | rF2TrackedDamage[512] | スロットごとのダメージ追跡 |
| `mInRealtimeFC` | bool | FC（Enter/ExitRealtime）でのリアルタイム状態 |
| `mMultimediaThreadStarted` | bool | マルチメディアスレッド起動済み |
| `mSimulationThreadStarted` | bool | シミュレーションスレッド起動済み |
| `mSessionStarted` | bool | セッション開始済み |
| `mTicksSessionStarted` | uint64 | セッション開始時のシステムティック |
| `mTicksSessionEnded` | uint64 | セッション終了時のシステムティック |
| `mSessionTransitionCapture` | rF2SessionTransitionCapture | セッション遷移時のスナップショット |
| `mDisplayedMessageUpdateCapture[128]` | char[128] | 表示メッセージ |
| `mDirectMemoryAccessEnabled` | bool | ダイレクトメモリアクセス有効 |
| `mTicksStatusMessageUpdated` | uint64 | ステータスメッセージ更新ティック |
| `mStatusMessage[128]` | char[128] | ステータスメッセージ |
| `mTicksLastHistoryMessageUpdated` | uint64 | 履歴メッセージ更新ティック |
| `mLastHistoryMessage[128]` | char[128] | 最後の履歴メッセージ |
| `mCurrentPitSpeedLimit` | float | m/s | 現在のピットレーン速度制限 |
| `mSCRPluginEnabled` | bool | Stock Car Rules プラグイン有効 |
| `mSCRPluginDoubleFileType` | int32 | SCR プラグインファイルタイプ |
| `mTicksLSIPhaseMessageUpdated` | uint64 | LSI フェーズメッセージ更新ティック |
| `mLSIPhaseMessage[96]` | char[96] | LSI フェーズメッセージ（レース状況指示） |
| `mTicksLSIPitStateMessageUpdated` | uint64 | LSI ピット状態メッセージ更新ティック |
| `mLSIPitStateMessage[96]` | char[96] | LSI ピット状態メッセージ |
| `mTicksLSIOrderInstructionMessageUpdated` | uint64 | LSI 順序指示メッセージ更新ティック |
| `mLSIOrderInstructionMessage[96]` | char[96] | LSI 順序指示メッセージ（追い抜き/ブロック指示） |
| `mTicksLSIRulesInstructionMessageUpdated` | uint64 | LSI ルール指示メッセージ更新ティック |
| `mLSIRulesInstructionMessage[96]` | char[96] | LSI ルール指示メッセージ |
| `mUnsubscribedBuffersMask` | int32 | 購読していないバッファのマスク |
| `mHWControlInputEnabled` | bool | ハードウェアコントロール入力有効 |
| `mWeatherControlInputEnabled` | bool | 天候コントロール入力有効 |
| `mRulesControlInputEnabled` | bool | ルールコントロール入力有効 |
| `mPluginControlInputEnabled` | bool | プラグインコントロール入力有効 |

### rF2TrackedDamage

| フィールド | 型 | 説明 |
|---|---|---|
| `mMaxImpactMagnitude` | double | 最大衝突強度 |
| `mAccumulatedImpactMagnitude` | double | 累積衝突強度（セッション通算） |

---

## 注意事項

- **`mBestSector2` と `mCurSector2` は S1+S2 の累積値**。S2単体の時間は `mBestSector2 - mBestSector1` で計算する
- **タイヤ温度は Kelvin**（摂氏変換: K - 273.15）
- **`mSector`（0=S3, 1=S1, 2=S2）** と直感に反する順序に注意
- **`_pack_=4` レイアウト**：LMU の共有メモリは ctypes の `_pack_=4` でアライメントされている。オフセット計算時は4バイト境界を意識する
- **`mLapNumber`** はラップ完了後にインクリメントされる。最初の走行は lap 1 からスタート

---

## KoDriver での実装済みフィールド一覧

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
| 位置 X/Y/Z | `mPos` | vehicleBase+160/168/176 |
| ローカル速度 X/Y/Z | `mLocalVel` | vehicleBase+184/192/200 |
| ブレーキ温度 | `mBrakeTemp` | wheelBase+24 |
| タイヤ空気圧 | `mPressure` | wheelBase+120 |
| タイヤ表面温度 | `mTemperature[1]` | wheelBase+136 |
| タイヤ摩耗 | `mWear` | wheelBase+152 |

---

## 参考リポジトリ

| リポジトリ | 言語 | 概要 |
|---|---|---|
| [TheIronWolfModding/rF2SharedMemoryMapPlugin](https://github.com/TheIronWolfModding/rF2SharedMemoryMapPlugin) | C++/C# | 公式プラグイン。rF2State.h が構造体定義の正典 |
| [s-victor/TinyPedal](https://github.com/s-victor/TinyPedal) | Python | 最も活発なLMU対応ツール。フィールド利用例が豊富 |
