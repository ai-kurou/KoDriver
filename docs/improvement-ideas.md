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

- **対象**: `.github/workflows/on-pull-request.yml` の `android-screenshot-test` ジョブ
  **課題**: `recordRoborazziAndroidHostTests` と `verifyRoborazziAndroidHostTests` を同一ジョブ内で連続実行しており、両者とも各モジュールの `testAndroidHostTest`（Androidホスト単体テスト全体、スクリーンショット以外も含む）に依存するため、`unit-test` ジョブ（`koverXmlReport` 経由）と合わせて実質同じテストスイートがCI全体で3回実行されている。さらに、CIが自動生成する `chore: update golden images` コミット（スナップショットPNGの一括更新）が入ると、多くのモジュールでGradleの `UP-TO-DATE` 判定が広範囲に無効化され、本来スキップできるテストが軒並み再実行されて実行時間が数分〜15分超まで跳ね上がることがある（PR #561で timeout-minutes を 10→15→25 に順次引き上げる事態が発生）。
  **改善案**: record用ジョブとverify用ジョブを分離する、あるいは差分のあるモジュールだけを対象に絞る、Roborazziタスクの入力宣言を見直してスナップショット全体ではなく変更されたファイル単位で正しく `UP-TO-DATE` 判定できるようにするなど、重複実行とキャッシュ無効化範囲の削減を検討する。

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
