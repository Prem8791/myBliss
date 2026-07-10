# Architecture Summary

## Current State

`com.home.launcher` is a Gradle-built Android application using AndroidX UI libraries. It currently works as a standalone platform-signed APK installed under `/data/app`. The app declares HOME intent filters and privileged task-management permissions. Recent-task behavior is currently implemented through hidden API reflection.

The source-level refactor in this preparation pass changes the architecture from direct `HiddenApi` calls to this boundary:

```text
MainActivity / RecentAppsAdapter
  -> RecentTasksRepository
      -> RecentTasksBackend
          -> ReflectionRecentTasksBackend      temporary Android 14 backend
          -> TaskOrganizerRecentTasksBackend   future backend placeholder
```

All remaining reflection is isolated under:

```text
app/src/main/java/com/home/launcher/system/hiddenapi/
```

Task-facing interfaces are under:

```text
app/src/main/java/com/home/launcher/task/
```

## Target First ROM Integration

Target first ROM state:

```text
/system/priv-app/HomeLauncher/HomeLauncher.apk
package=com.home.launcher
certificate=platform
privileged=true
hiddenApiEnforcementPolicy=0
SELinux domain=platform_app
HOME activity=com.home.launcher/.MainActivity
```

The first ROM integration is not a full Overview/QuickStep replacement. It makes the launcher a privileged platform app and keeps the temporary reflection backend.

## Target Final Architecture

Final architecture should replace reflection with:

```text
TaskOrganizer / WM Shell callbacks
  -> RecentTasksRepository
      -> launcher UI
```

SystemUI Overview should eventually bind to `com.home.launcher` through:

```text
config_recentsComponentName
OverviewProxyService
android.intent.action.QUICKSTEP_SERVICE
IOverviewProxy / ISystemUiProxy
```

## Mandatory Changes

- Add Soong `android_app` with `platform_apis: true`, `certificate: "platform"`, and `privileged: true`.
- Include AndroidX static libraries needed by the current app.
- Add privapp permission allowlist.
- Add product package entries.
- Set the launcher as default HOME after flashing if Launcher3 remains installed.

## Optional Future Improvements

- Remove or replace Launcher3QuickStep.
- Enable `HomeLauncherConfigOverlay`.
- Implement QuickStep service compatibility.
- Implement TaskOrganizer backend.
- Remove reflection backend.
- Replace deprecated `LocalBroadcastManager`.
