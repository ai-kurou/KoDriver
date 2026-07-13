# 改善案メモ

作業中に思いついた改善案（実装・設計・UI/UX・テスト・ドキュメント・開発体験など）を書き残すファイル。

- 依頼タスクの範囲外でも、気づいた時点で追記する。このファイルへの記録は「記録」であり、対象コードの変更やテスト追加を意味しない。
- 各項目は「対象・課題・改善案」が後から読んで分かる粒度で、箇条書きで書く。関連するファイル・モジュール名があれば添える。
- 実際に着手・解決した項目は、対応する PR 番号などを添えて整理・削除してよい。

記録の書式例:

```
## <カテゴリ>

- **対象**: <ファイル / モジュール / 画面など>
  **課題**: <現状の問題・気になっている点>
  **改善案**: <どう変えたいか>
```

---

## CI

- **対象**: `.github/workflows/on-pull-request.yml` / `on-main-merge.yml` の `desktop-screenshot-test` / `android-screenshot-test` ジョブ
  **課題**: `recordRoborazziXxxTests` と `verifyRoborazziXxxTests` を同一ジョブ内で連続実行しており、両者とも各モジュールの `testJvmTest` / `testAndroidHostTest`（スクリーンショット以外も含む単体テスト全体）に依存するため、`unit-test` ジョブ（`koverXmlReport` 経由）と合わせて実質同じテストスイートがCI全体で3回実行されている。さらに、CIが自動生成する `chore: update golden images` コミット（スナップショットPNGの一括更新）が同一ジョブ内の `verify` ステップ実行前に発生すると、多くのモジュールでGradleの `UP-TO-DATE` 判定が広範囲に無効化され、本来スキップできるテストが軒並み再実行されて実行時間が数分〜15分超まで跳ね上がることがある（PR #561で timeout-minutes を 10→15→25 に順次引き上げる事態が発生）。
  **改善案（一部対応済み）**: record用ジョブとverify用ジョブを分離した（`xxx-screenshot-test-record` → `needs:` で依存する `xxx-screenshot-test-verify`）。verify側は golden image コミット後のHEADをcheckoutしてから実行するため、goldenイメージ更新コミット自体によるキャッシュ無効化の影響を受けにくくなった。ただし `unit-test` ジョブとの重複実行（同じテストスイートがCI全体で複数回走る点）は未解消であり、Roborazziタスクの入力宣言を見直してスナップショット全体ではなく変更されたファイル単位で正しく `UP-TO-DATE` 判定できるようにするなど、引き続き重複実行の削減を検討する。なお、本改善（PR #566）はYAML分離のみの変更で導入したため、実際にgolden imageが更新されるPRでの実行時間短縮効果はまだ未検証。次にスクリーンショット対象UIを変更するPRで実行時間を確認し、効果が確認でき次第この項目を整理する。

## core:lmu-windows-data

- **対象**: `core/lmu-windows-data/.../mapper/LmuWindowsMapper.kt`、`core/domain/.../model/LmuWindowsTimingData.kt`
  **課題**: `LmuWindowsTimingData.sector2Ms` には共有メモリの `mBestLapSector2` を格納しているが、このフィールドは S1+S2 の累積値であり、セクター2単体のタイムではない。フィールド名からは S2 単体に見えるため、将来これを表示・読み上げに使うと誤ったタイムを扱うバグになる（現時点で `sector1Ms` / `sector2Ms` を消費する実装はなく実害は未発生）。また `mBestLapSector1/2` は「ベストセクタータイム」ではなく「ベストラップ中のセクタータイム」である点も要注意。
  **改善案**: 消費側を実装するときに `sector2Ms - sector1Ms` で S2 単体を算出するか、フィールド名を `sector1And2Ms` などの累積値であることが分かる名前に変更する。詳細は `docs/lmu-windows-telemetry.md` の注意事項を参照。

- **対象**: `core/lmu-windows-data/.../mapper/LmuWindowsMapper.kt`
  **課題**: `MAX_SCORING_VEHICLES = 128` は rF2 プラグイン由来の値で、LMU の `vehScoringInfo` 配列は 104 要素（`MAX_MAPPED_VEHICLES`）。`mNumVehicles` が万一 104 を超える値だった場合、105 台目以降の探索は配列末尾を越えて `scoringStream` 領域を車両データとして読むことになる（実際に LMU が 104 超を返す可能性は低く、実害はほぼない）。
  **改善案**: `MAX_SCORING_VEHICLES` を 104 に修正する。

## mDNS（server / feature:other-server-ip-detail）

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`
  **課題**: `start()` は既存の `JmDNS` インスタンスを close せずに新規生成して上書きする。現状 `KoDriverServer.start()` は起動時に1回しか呼ばれないため実害はないが、将来サーバー再起動フローができた場合に前のマルチキャストソケットがリークする。
  **改善案**: `start()` 冒頭で既存インスタンスがあれば `stop()` を呼ぶ、または多重起動を防ぐガードを入れる。

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`、`feature/other-server-ip-detail/src/jvmMain/kotlin/kurou/kodriver/feature/otherserveripdetail/PlatformWindowsServerDiscovery.jvm.kt`
  **課題**: mDNSサービスタイプ文字列 `_kodriver._tcp.local.` がサーバー側・クライアント側それぞれに private 定数として個別定義されており、共有定数化されていない。将来どちらかだけ値を変更すると検出できなくなる。
  **改善案**: 両モジュールから参照できる共通定数（例: `core:domain` 等）へ抽出する。

- **対象**: `server/src/main/kotlin/kurou/kodriver/KoDriverServiceAdvertiser.kt`
  **課題**: mDNSのインスタンス名に `InetAddress.getLocalHost().hostName` を使用しているが、環境によってはFQDN（ドット区切り）が返ることがあり、mDNSサービス名にドットが含まれると `ServiceInfo.create` での名前解釈が崩れる可能性がある。
  **改善案**: ホスト名のサニタイズ（ドット除去など）を検討する。

- **対象**: `feature/other-server-ip-detail/src/androidMain/kotlin/kurou/kodriver/feature/otherserveripdetail/PlatformWindowsServerDiscovery.android.kt`
  **課題**: `NsdManager` ベースの検出実装で、実機のDoze/Wi-Fiスリープ設定次第では検出が不安定になる可能性がある既知の事例がある。また、プラットフォーム固有の外部APIを直接呼ぶためユニットテストの対象外（`CLAUDE.md`のテスト方針に基づく除外）となっており、実機確認でしか動作を担保できない。
  **改善案**: Doze/Wi-Fiスリープ中の実機動作確認を行う。必要であれば結合テスト（instrumented test）の追加を検討する。

## ViewModel / UseCase 責務分離

- **対象**: `feature/readout-list/.../ReadoutListViewModel.kt`
  **課題**: 保存済み読み上げ順序と現在のデフォルト順序を突き合わせ、削除済み項目を除外し、新規項目を末尾に補完するロジックが ViewModel 内にある。これは UI 表示都合だけでなく、読み上げ項目順序の整合性を保つドメインルールに近い。
  **改善案**: `ResolveReadoutOrderUseCase` などへ切り出し、`ObserveReadoutOrderUseCase` の結果と `ReadoutListItemType.defaultOrder(simulator)` から有効な順序を生成する責務を domain 側へ寄せる。

- **対象**: `feature/telemetry-log-list/.../TelemetryLogListViewModel.kt`
  **課題**: テレメトリログの新しい順ソートと、削除後などに存在しなくなった選択IDを無効化する処理が ViewModel 内にある。現状は小さいが、ログ検索・フィルタ・ページングを追加すると ViewModel の表示整形責務が膨らみやすい。
  **改善案**: 必要になった段階で `ObserveSortedTelemetryLogsUseCase` やログ一覧用の query UseCase へ切り出し、ViewModel は選択状態とダイアログ状態だけを扱う。

- **対象**: `feature/other-server-ip-detail/.../OtherServerIpDetailViewModel.kt`
  **課題**: IPv4 形式チェック、接続確認付き保存、接続警告後の強制保存が ViewModel 内にある。画面専用の入力処理としては許容範囲だが、接続先設定の保存ルールとして再利用される場合は責務が重くなる。
  **改善案**: 他画面や自動設定で再利用する段階で `ValidateIpAddressUseCase` や `SaveServerIpWithConnectivityCheckUseCase` へ切り出す。

- **対象**: `feature/gt7-ps5-narrator/.../Gt7Ps5NarratorViewModel.kt`
  **課題**: 読み上げ判定自体は `DetermineGt7Ps5NarratorReadoutUseCase` に切れているが、優先度に基づく読み上げ中断判定、前回テレメトリとのログJSON生成、機能ごとの前回値保持が ViewModel に残っている。GT7の読み上げ項目が増えると LMU Narrator と同様に肥大化しやすい。
  **改善案**: 読み上げ優先度制御やログ保存を担う小さな UseCase / service へ段階的に切り出し、ViewModel は Flow の接続とライフサイクル管理に寄せる。
