# OSSのためソース自体が公開されており、識別子難読化によるリバースエンジニアリング防止のメリットが薄い。
# 一方でクラッシュログをmapping.txtなしでそのまま読めるようにするため、難読化のみ無効化する
# （未使用コード削除・最適化によるサイズ/メモリ削減効果はminifyEnabledの範囲で引き続き有効）。
-dontobfuscate

# core:windows-startup-dataはWindows版デスクトップアプリ専用の機能で、Android版からは到達しない
# コードパスだがandroidApp経由でクラスパスに含まれるため、R8がjava.lang.ProcessHandleを
# 解決できず警告になる（Android SDKにこのクラスは存在しない）。
-dontwarn java.lang.ProcessHandle
-dontwarn java.lang.ProcessHandle$Info
