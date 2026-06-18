# Windows 版のインストール手順

## インストール

1. [Releases](https://github.com/ai-kurou/KoDriver/releases) から最新の `KoDriver-windows-*.msi` をダウンロードします。
2. ダウンロードした MSI インストーラーを実行します。
3. インストール後、スタートメニューまたはデスクトップショートカットから KoDriver を起動します。

LMU が起動していない状態で KoDriver を起動しても問題ありません。KoDriver は LMU の起動を検知すると自動的に接続します。

## Windows SmartScreen の警告

現在配布している Windows 版インストーラーはコード署名されていません。そのため、Windows SmartScreen やブラウザのダウンロード保護で警告が表示される場合があります。

SmartScreen の警告が表示された場合は、配布元がこのリポジトリの [Releases](https://github.com/ai-kurou/KoDriver/releases) であることを確認してください。内容に問題がなければ、警告画面の「詳細情報」から実行できます。

## Android アプリと連携する場合

Android アプリからデスクトップアプリに接続する場合は、Windows PC と Android 端末を同じ LAN に接続してください。

デスクトップアプリは同一プロセス内で KoDriver サーバーを起動し、TCP `8080` 番ポートで待ち受けます。Android 端末から接続できない場合は、Windows ファイアウォールで TCP `8080` の受信が許可されているか確認してください。

KoDriver サーバーは認証・暗号化を実装していないため、信頼できる LAN 内でのみ使用してください。

## アンインストール

Windows の「設定」から「アプリ」または「インストールされているアプリ」を開き、KoDriver を選択してアンインストールしてください。

## 既知の制限

- Windows 共有メモリを利用するため、LMU との接続は Windows 版デスクトップアプリでのみ動作します。
- Android アプリ単体では LMU の共有メモリを直接読み取れません。デスクトップアプリとの LAN 接続が必要です。
