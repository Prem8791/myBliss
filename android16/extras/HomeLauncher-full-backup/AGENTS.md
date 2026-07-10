# Build, Sign & Install

## Build
```
.\gradlew assembleDebug
```

## Sign with platform keys
```
apksigner sign --key platform.pk8 --cert platform.x509.pem --out app\build\outputs\apk\debug\app-platform-signed.apk app\build\outputs\apk\debug\app-debug.apk
```
Keys are in the project root: `platform.pk8` and `platform.x509.pem`.

## Fresh install (uninstall first)
```
adb uninstall com.home.launcher
adb install app\build\outputs\apk\debug\app-platform-signed.apk
```

## Incremental install (replace)
```
adb install -r app\build\outputs\apk\debug\app-platform-signed.apk
```

## Restart (after install)
```
adb shell am force-stop com.home.launcher && adb shell am start -n com.home.launcher/.MainActivity
```

## Combined quick cycle (incremental)
```
.\gradlew assembleDebug && apksigner sign --key platform.pk8 --cert platform.x509.pem --out app\build\outputs\apk\debug\app-platform-signed.apk app\build\outputs\apk\debug\app-debug.apk && adb install -r app\build\outputs\apk\debug\app-platform-signed.apk && adb shell am force-stop com.home.launcher && adb shell am start -n com.home.launcher/.MainActivity
```

## ADB path
`C:\Users\Steno\AppData\Local\Android\Sdk\platform-tools\adb.exe`

## Apksigner path
`C:\Users\Steno\AppData\Local\Android\Sdk\build-tools\36.1.0\apksigner.bat`
