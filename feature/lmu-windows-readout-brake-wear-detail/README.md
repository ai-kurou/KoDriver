# lmu-windows-readout-brake-wear-detail

LMU のブレーキ摩耗の実機調査用の詳細画面。LMU 内蔵 REST API の `wearables.brakes`（`/rest/garage/UIScreen/RepairAndRefuel`）と `/rest/garage/brakeinfo` の生の4輪配列（FL, FR, RL, RR の順と推測）を1秒間隔で表示し、どちらがブレーキ摩耗を表すかを走行しながら確認できるようにする。

「現在値を基準にする」を押すと、その時点の値を基準として各輪の差分を表示する（ブレーキ交換前後や周回後の変化量の確認用）。基準はこの画面の間だけ保持し、永続化しない。

REST API は LMU を起動した Windows 機の `localhost` にしか待ち受けないため、値を取得できるのはデスクトップ版のみ。Android や LMU 未起動時、取得に失敗した項目は「取得できません」と表示する。

読み上げの設定項目（しきい値・文言・試聴など）は、摩耗を表す値が確定してから別途追加する。調査結果は [`docs/lmu-windows-rest-api.md`](../../docs/lmu-windows-rest-api.md) に反映する。

<!-- MODULE-GRAPH-START -->
## Module Dependencies

![Module Graph](../../docs/graphs/feature-lmu-windows-readout-brake-wear-detail.svg)
<!-- MODULE-GRAPH-END -->
