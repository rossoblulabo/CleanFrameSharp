# Privacy

English · [日本語](docs/ja/PRIVACY.md)

CleanFrame Sharp runs locally and requests no Internet or network-state permission. It contains no advertising SDK, analytics SDK, telemetry uploader, account system or crash-reporting service.

## Data on the TV

The app saves your selected layout, Home app order, background choice, typography, focus preferences, language and Home-interception choice in its private local storage. A custom photo is opened only after you choose it with Android’s document picker; the app retains the chosen URI permission. Installed launchable app names, icons and selected package enabled states are read locally to display the interface. Package status is refreshed on request, not continuously monitored. Android backup is disabled for this app.

## Optional Home Accessibility service

Android delivers key events to the service while enabled. Its implementation handles only Home (key code 3); other keys pass through without recording their values, text or timing. It does not request screen-content retrieval, window events, gestures or touch exploration. A short Home-handling result is held in memory for the current service session. An enable preference and, if necessary, a generic launch-failure message are saved locally. Nothing is transmitted.

Pause interception in the app or disable **CleanFrame Home button** in TV Accessibility settings to stop interception. Other Accessibility services and system packages are not changed by this app. The launcher works as an ordinary app without Accessibility access.

## External apps and removal

Apps you launch, the TV operating system and the document picker have their own privacy behavior. CleanFrame cannot control their network activity. Clearing this app’s storage or uninstalling it removes its local settings; your original launcher and other apps remain installed.

## Public reports

The public source and APK contain no private device address, account profile, personal photograph or device diagnostic dump. The reported model/OS compatibility and fictional example addresses are deliberately documented. Changing the app language does not change the TV’s language or installed apps’ labels. Do not post unredacted logs or screenshots showing personal information. Report only the minimal device and error details needed to reproduce an issue.
