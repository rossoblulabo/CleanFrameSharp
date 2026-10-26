# Install and recover — Windows

English · [日本語](docs/ja/INSTALL-AND-RECOVERY.md)

This guide uses a Windows PC, your TV remote and the prebuilt APK. You do not need Android Studio, root or a permanent PC connection. Keep the original launcher installed and enabled.

## 1. Download the two files

1. Download [Android SDK Platform Tools for Windows](https://developer.android.com/tools/releases/platform-tools).
2. Right-click the downloaded ZIP and choose Extract All.
3. Open the extracted platform-tools folder. Check that adb.exe is inside it.
4. Download CleanFrameSharp-1.1.0.apk from this project’s Releases page.
5. Copy that APK into the same platform-tools folder, next to adb.exe.

You now have adb.exe and the APK in one folder. This makes the install command below work without entering a long file path.

## 2. Enable developer debugging on the TV

1. Open the TV’s Settings (設定).
2. Open About / Device information (端末情報). If it is inside Device Preferences or Other settings on your firmware, open that menu first.
3. Scroll to Build / Build number (ビルド / ビルド番号).
4. With Build selected, press the remote’s OK/決定 button **seven times**. Stop when the TV says developer options are enabled.
5. Return to Settings and open Developer options (開発者向けオプション). It may be under Other settings / Device Preferences.
6. Turn USB debugging (USBデバッグ) On. Accept the confirmation.
7. If your TV also offers Network debugging / ADB debugging, turn that on and note its port.

The item to press seven times is **Build**, not About itself. If the TV says you are already a developer, continue to step 5. Menu names vary with firmware. Leave other developer options unchanged.

This guide uses network ADB. Some TVs expose it on port 5555 when debugging is enabled; other TVs need a manufacturer-supported network-debugging method. Enabling USB debugging alone does not guarantee a network connection. If connection is refused later, check that your TV actually supports network ADB. Android 10 does not have the newer wireless pairing-code menu.

## 3. Find the TV’s address

1. Keep the TV and PC on the same home network.
2. Open the TV’s Settings → About / Device information → Status (ステータス).
3. Note its IPv4 address. It normally looks like 192.168.1.50.
4. Use the ADB port shown by the TV’s debugging method; the usual legacy network port is 5555.

The address **192.168.1.50:5555** throughout this guide is an example, not a device recorded by this project. Replace it with your own TV’s IP address and debugging port every time. Do not use the router’s address. Your TV’s address may change after reconnecting.

## 4. Open PowerShell and connect

1. On the PC, open the platform-tools folder in File Explorer.
2. Click File Explorer’s address bar.
3. Type <strong>powershell</strong> and press Enter.
4. Run this one line:

~~~powershell
.\adb.exe connect 192.168.1.50:5555
~~~

Replace 192.168.1.50:5555 with your TV’s address and port. PowerShell is now working in the folder you opened; .\ means that folder.

5. On the TV, accept Allow USB debugging? (USBデバッグを許可しますか？). Approve only your own PC.
6. Check the connection:

~~~powershell
.\adb.exe devices
~~~

Continue when your TV’s row ends with **device**. If it says unauthorized, accept the TV prompt. If offline or connection refused, check the address, network and debugging settings before installing. Run commands one at a time; do not paste the explanatory paragraphs. If PowerShell shows a continuation prompt (>>), press Ctrl+C and enter the command again as one complete line.

## 5. Install or update

1. Run:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 install -r ".\CleanFrameSharp-1.1.0.apk"
~~~

Replace the address and port. The APK path above means the file is in the folder where you opened PowerShell. Change the filename if you downloaded another version. The -r option updates an existing copy and keeps its preferences when the signing certificate matches. Do not uninstall first.

If the APK is in another folder, use its full path instead:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 install -r "C:\TVApps\CleanFrameSharp-1.1.0.apk"
~~~

Replace the address and port, and replace C:\TVApps\CleanFrameSharp-1.1.0.apk with the APK’s actual location. Keep quotation marks around a path containing spaces. Use one install command, not both.

2. Wait for **Success**.
3. Open CleanFrame Sharp from the TV’s Apps screen. Or run:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell am start -n org.cleanframe.sharp/.MainActivity
~~~

Replace only the address and port. The package/activity text identifies CleanFrame and should stay as written.

4. Test All Apps, launching an app, Settings and Back.
5. If desired, select **Settings → Language / 言語 → 日本語**. Installed app names stay as supplied by those apps.

If installation says INSTALL_FAILED_UPDATE_INCOMPATIBLE, the installed copy has a different signing certificate. Obtain the matching update. Removing only CleanFrame allows a different certificate but deletes its preferences; return to the original launcher before doing that. Do not remove a stock launcher.

## 6. Enable the Home-button service first

1. On the TV, open **設定 → その他の設定 → ユーザー補助**.
2. Scroll to the bottom, to the **サービス** heading.
3. Select **CleanFrame Home button** and turn it On. Accept Android’s permission explanation if you choose to use this feature.
4. Return to CleanFrame Sharp.
5. Open **Settings → Home button → Turn on Home interception**.
6. Select Refresh status.
7. Open another app and press Home once. It should open CleanFrame on compatible firmware.

The English menu names are Settings → Other settings → Accessibility → Services. The path above was reported on the tested Sharp firmware; other TVs may differ. Enable the TV service **before** turning on Home interception. Do not use a competing Home remapper at the same time. Both short and held Home presses open CleanFrame while enabled, replacing the original long-press action. You can still use CleanFrame as an ordinary app if you skip this section.

> **IMPORTANT — OPENING GOOGLE LAUNCHER TURNS HOME INTERCEPTION OFF**
>
> **Settings → Home button → Open Google launcher** shows a warning. If you choose OK, CleanFrame turns **Home interception OFF** and opens the original Google launcher. Home will then open Google’s launcher.
>
> To make Home open CleanFrame Sharp again, open CleanFrame → **Settings → Home button → Turn on Home interception**. It will remain off until you turn it on again. Cancel leaves the setting unchanged.

## 7. Finish and turn debugging off

1. Confirm that Home opens CleanFrame from another app and that Back, volume, input selection and your normal TV functions work.
2. Keep the recovery instructions below available.
3. On the TV, open **Settings → Developer options → USB debugging → Off**. Use Other settings / Device Preferences first if that is where Developer options appears on your firmware.
4. If you separately enabled Network debugging / ADB debugging, turn that Off in the same developer menu or the tool you used to enable it.
5. Leave **CleanFrame Home button** enabled in Accessibility and **Home interception** enabled inside CleanFrame if you want Home to keep opening it.
6. You may close PowerShell and disconnect the PC.
7. Test Home from another app once more.

That is all that is required. CleanFrame does not need a permanent ADB connection, root, another remapping app or disabled system packages. Do not turn the Accessibility service off as part of finishing.

## Recovery: return to the original launcher

1. In CleanFrame, open **Settings → Home button → Open Google launcher**.
2. Read the warning and choose OK.

Home interception is now Off. The original launcher remains available. If you separately changed Android’s default Home app, restore the original app in that chooser too; CleanFrame does not change that setting for you.

If CleanFrame is difficult to reach, open the TV’s **設定 → その他の設定 → ユーザー補助 → サービス → CleanFrame Home button → Off**. Then press Home. Use the TV’s own Settings button/menu if available.

If debugging is still enabled and connected, you can request the main Settings screen:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell am start -a android.settings.SETTINGS
~~~

Replace the address and port. This opens a screen; use the remote to change the service. Some firmware may not support this shortcut. If debugging was switched off, re-enable it using section 2 before using ADB again.

As a last resort, remove **only CleanFrame Sharp**:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 uninstall org.cleanframe.sharp
~~~

Replace the address and port. Uninstalling deletes CleanFrame’s preferences and removes its Accessibility service. It does not remove other apps. Press Home and select the original launcher if Android asks. Never disable a stock launcher or clear system-app data as recovery.

## Optional extras

[Optional system-service guide](OPTIONAL-SERVICES.md): not required for CleanFrame. [Privacy](PRIVACY.md). [Google’s developer-options instructions](https://developer.android.com/studio/debug/dev-options) confirm the seven presses; [Google’s TV debugging guide](https://developers.google.com/cast/docs/android_tv_receiver/debugging) describes network ADB and authorization. Firmware menus may differ from the original manual.
