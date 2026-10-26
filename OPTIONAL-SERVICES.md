# Optional: disable selected system services

English · [日本語](docs/ja/OPTIONAL-SERVICES.md)

**This is not required to install or use CleanFrame Sharp.** These are system packages, not just background processes. Disabling one can remove features and stop its receivers/services from running while it remains disabled. Do not treat every background service as unnecessary.

## What was actually reported

On the reported Sharp AQUOS 4T-C45BL1 / AQUOS-TVJ19, Android TV 10 setup, normal use was reported without noticed problems with these three disabled:

- COCORO VISION — jp.co.sharp.av.android.cocorovision.
- Android TV Core Services / TV Recommendations — com.google.android.tvrecommendations.
- Sharp Analytics — jp.co.sharp.av.android.analyticsservice.

This is one setup’s experience, not a guarantee about all features, other TVs, firmware or future updates. It does not establish a startup-performance improvement. Test the functions you use after each change.

The same package listing also showed **jp.co.sharp.av.android.monitormaintainer** and **jp.co.sharp.av.android.tvlauncher** disabled. Those states predated this project. They are recorded as existing conditions, not steps to reproduce. This guide does not recommend disabling either of them or provide disable commands for them. Google Feedback and TV Bug Report Sender were discussed but were not shown disabled in the supplied listing.

## Before changing anything

1. Read the [installation and recovery guide](INSTALL-AND-RECOVERY.md).
2. If debugging was turned off, repeat its developer-debugging and connection steps.
3. Open PowerShell in the folder containing adb.exe and connect to your TV.
4. Check the current disabled list:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm list packages -d
~~~

Replace 192.168.1.50:5555 with your TV’s address and debugging port in every example. These commands apply to Android user 0; do not copy them to a different user/profile without checking. Keep a note of the state before you start, without sharing personal identifiers publicly.

5. Change **one package at a time**. Use only the section you have chosen below.
6. After disabling it, test the TV, then restart it and test again. If anything stops working, use that section’s enable command immediately.

The app’s package-status screen is read-only. These commands run only because you enter them in PowerShell. Do not uninstall system packages or clear their data.

## COCORO VISION

This provides Sharp’s COCORO VISION recommendations and integrations. Disabling it removes those features and may affect dependent Sharp functions. It is not necessary for the CleanFrame interface.

To disable:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm disable-user --user 0 jp.co.sharp.av.android.cocorovision
~~~

Replace the address and port. Leave the package name unchanged. Expect COCORO VISION features to become unavailable.

To enable again:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm enable --user 0 jp.co.sharp.av.android.cocorovision
~~~

Replace the address and port. This re-enables the installed package without uninstalling it or deleting its data.

## Android TV Core Services / TV Recommendations

Despite the package name, this can provide more than recommendation rows: stock Home integrations, notifications and promotional content may be affected. Disabling it can alter Google’s original launcher experience and other integrations. Keep Google’s launcher enabled as a recovery route.

To disable:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm disable-user --user 0 com.google.android.tvrecommendations
~~~

Replace the address and port. Leave the package name unchanged. Check notifications and the original Google launcher as well as CleanFrame.

To enable again:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm enable --user 0 com.google.android.tvrecommendations
~~~

Replace the address and port. Recheck the affected functions; restart the TV if they do not return immediately.

## Sharp Analytics

This package is associated with Sharp usage analytics. Other Sharp software may depend on its providers or integrations; it is not a proven isolated switch for every form of telemetry on the TV.

To disable:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm disable-user --user 0 jp.co.sharp.av.android.analyticsservice
~~~

Replace the address and port. Leave the package name unchanged. Check your normal TV and Sharp functions afterwards.

To enable again:

~~~powershell
.\adb.exe -s 192.168.1.50:5555 shell pm enable --user 0 jp.co.sharp.av.android.analyticsservice
~~~

Replace the address and port. Re-enabling preserves the installed package and its data.

## Finish

1. Run the disabled-list command again and check that only the intended changes were made.
2. Test live TV, channel changes, the programme guide, recording/reservations/playback, broadcast data services, HDMI/CEC, remote keys, streaming sign-in/playback, casting, notifications and the original launcher where you use them.
3. Restart the TV through its normal menu, then test again. Do not power it off while it is recording or updating.
4. Re-enable a package if you find a problem. The enable commands restore availability, not a promise that every dependent process has already restarted.
5. Turn debugging off using the installation guide’s Finish section.

The disabled state normally survives reboot, so the package is not started normally at each boot. Firmware updates or factory resets may change that state; check again afterwards. Keep Google’s launcher, Play Services, Play Store, Assistant, tuner, EPG, recording and broadcast middleware enabled. Do not disable packages just to force Home-button routing.
