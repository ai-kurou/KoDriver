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

## ViewModel / UseCase 責務分離

- **対象**: `feature/telemetry-log-list/.../TelemetryLogListViewModel.kt`
  **課題**: テレメトリログの新しい順ソートと、削除後などに存在しなくなった選択IDを無効化する処理が ViewModel 内にある。現状は小さいが、ログ検索・フィルタ・ページングを追加すると ViewModel の表示整形責務が膨らみやすい。
  **改善案**: 必要になった段階で `ObserveSortedTelemetryLogsUseCase` やログ一覧用の query UseCase へ切り出し、ViewModel は選択状態とダイアログ状態だけを扱う。

- **対象**: `feature/other-server-ip-detail/.../OtherServerIpDetailViewModel.kt`
  **課題**: IPv4 形式チェック、接続確認付き保存、接続警告後の強制保存が ViewModel 内にある。画面専用の入力処理としては許容範囲だが、接続先設定の保存ルールとして再利用される場合は責務が重くなる。
  **改善案**: 他画面や自動設定で再利用する段階で `ValidateIpAddressUseCase` や `SaveServerIpWithConnectivityCheckUseCase` へ切り出す。

- **対象**: `feature/gt7-ps5-narrator/.../Gt7Ps5NarratorViewModel.kt`
  **課題**: 読み上げ判定自体は `DetermineGt7Ps5NarratorReadoutUseCase` に切れているが、優先度に基づく読み上げ中断判定、前回テレメトリとのログJSON生成、機能ごとの前回値保持が ViewModel に残っている。GT7の読み上げ項目が増えると LMU Narrator と同様に肥大化しやすい。
  **改善案**: 読み上げ優先度制御やログ保存を担う小さな UseCase / service へ段階的に切り出し、ViewModel は Flow の接続とライフサイクル管理に寄せる。
