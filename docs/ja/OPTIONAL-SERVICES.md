# 任意設定：一部のシステムサービスを無効にする

[English](../../OPTIONAL-SERVICES.md) · 日本語

**CleanFrame Sharpのインストールや利用に、この操作は必要ありません。** 対象は単なる実行中プロセスではなく、システムパッケージです。無効にすると、そのパッケージの機能やサービスが使えなくなります。バックグラウンドで動くものがすべて不要という意味ではありません。

## 実際に報告された状態

Sharp AQUOS 4T-C45BL1（AQUOS-TVJ19）、Android TV 10の環境で、次の3つを無効にした状態でも、通常の利用では問題に気付いていないとの報告がありました。

- COCORO VISION：jp.co.sharp.av.android.cocorovision
- Android TV Core Services／TV Recommendations：com.google.android.tvrecommendations
- シャープの利用状況解析：jp.co.sharp.av.android.analyticsservice

1台の利用報告であり、すべての機能や他機種、別のファームウェア、将来の更新での動作を保証するものではありません。起動速度が改善したと確認できたわけでもありません。変更するたびに、ご自身で使う機能を確認してください。

同じ一覧では、**jp.co.sharp.av.android.monitormaintainer**と**jp.co.sharp.av.android.tvlauncher**も無効でした。この2つはプロジェクト開始前からの状態です。再現するための手順ではなく、このガイドでは無効化を勧めず、無効化コマンドも掲載しません。GoogleフィードバックとTV Bug Report Senderは検討されましたが、提示された無効一覧にはありませんでした。

## 作業前の準備

1. [インストールと復旧のガイド](INSTALL-AND-RECOVERY.md)を読んでください。
2. デバッグをオフにしていた場合は、そのガイドに従って再度有効にし、接続します。
3. adb.exeのあるフォルダーでPowerShellを開きます。
4. 現在の無効一覧を確認します。

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm list packages -d
~~~

以後の例では毎回、192.168.1.50:5555をご自身のテレビのIPアドレスとデバッグポートに置き換えます。コマンドはAndroidのユーザー0を対象とします。別のユーザーやプロファイルでは、対象を確認せずに使わないでください。作業前の状態を控え、個人情報を含む記録は公開しないでください。

5. **1つずつ**変更し、下の必要な項目だけを実行します。
6. 変更後に動作を確認し、再起動後にも確認します。不具合があれば、その項目の有効化コマンドですぐに戻します。

CleanFrameのパッケージ画面は状態を表示するだけです。以下のコマンドは、PowerShellで自分で実行したときだけ動きます。システムパッケージの削除やデータ消去はしないでください。

## COCORO VISION

シャープのおすすめ表示や連携機能に使われます。無効にすると、それらが使えなくなり、依存するシャープの機能にも影響する可能性があります。CleanFrameの画面表示には必要ありません。

無効にする：

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm disable-user --user 0 jp.co.sharp.av.android.cocorovision
~~~

IPアドレスとポート番号だけを置き換え、パッケージ名は変えません。COCORO VISIONの機能は利用できなくなります。

有効に戻す：

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm enable --user 0 jp.co.sharp.av.android.cocorovision
~~~

IPアドレスとポート番号を置き換えます。パッケージや保存データを削除せずに、有効に戻します。

## Android TV Core Services／TV Recommendations

名前にRecommendationsとありますが、おすすめ欄だけの機能とは限りません。標準ホームの連携、通知、プロモーション表示などにも影響する可能性があります。Googleの元のランチャーや連携機能が変わる場合があります。復帰手段としてGoogleランチャー自体は有効のままにしてください。

無効にする：

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm disable-user --user 0 com.google.android.tvrecommendations
~~~

IPアドレスとポート番号を置き換えます。CleanFrameだけでなく、通知や元のGoogleランチャーも確認してください。

有効に戻す：

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm enable --user 0 com.google.android.tvrecommendations
~~~

IPアドレスとポート番号を置き換えます。機能がすぐに戻らない場合は、テレビを再起動して確認してください。

## シャープの利用状況解析

シャープの利用状況解析に関連するパッケージです。他のシャープ製ソフトウェアがプロバイダーや連携機能に依存している可能性があります。これだけでテレビ上のすべての情報送信を止められると確認されたわけではありません。

無効にする：

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm disable-user --user 0 jp.co.sharp.av.android.analyticsservice
~~~

IPアドレスとポート番号だけを置き換えます。普段使うテレビ機能とシャープの機能を確認してください。

有効に戻す：

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm enable --user 0 jp.co.sharp.av.android.analyticsservice
~~~

IPアドレスとポート番号を置き換えます。インストール済みパッケージと保存データは維持されます。

## 作業を終える

1. 無効一覧をもう一度表示し、意図したものだけが変わったことを確認します。
2. 放送、選局、番組表、録画・予約・再生、データ放送、HDMI/CEC、リモコン、動画配信のログインと再生、キャスト、通知、元のランチャーなど、使う機能を確認します。
3. テレビの通常のメニューから再起動し、もう一度確認します。録画中や更新中は電源を切らないでください。
4. 問題があれば有効に戻します。有効化はパッケージを利用可能にする操作であり、依存するすべての処理が即座に再起動する保証ではありません。
5. インストールガイドの終了手順に従い、デバッグをオフにします。

通常、無効状態は再起動後も維持され、そのパッケージは通常の方法では起動しなくなります。ファームウェア更新や初期化で状態が変わる場合があるため、その後は再確認してください。Googleランチャー、Play開発者サービス、Playストア、アシスタント、チューナー、番組表、録画、放送関連の基盤機能は有効のままにします。ホームボタンの割り当てを強制するために、パッケージを無効にしないでください。
