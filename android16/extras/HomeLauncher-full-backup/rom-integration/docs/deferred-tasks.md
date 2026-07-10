# Intentionally Deferred Tasks

Deferred until after `com.home.launcher` boots successfully as a ROM-bundled privileged platform app:

1. Implement TaskOrganizer backend.
2. Implement `android.intent.action.QUICKSTEP_SERVICE`.
3. Enable `HomeLauncherConfigOverlay`.
4. Replace Launcher3/QuickStep as official Overview provider.
5. Remove Launcher3QuickStep from product packages.
6. Add/default-grant notification listener access.
7. Add/default-grant calendar permission.
8. Apply SELinux policy for `/proc/stat` and thermal sysfs.
9. Replace deprecated `LocalBroadcastManager`.
10. Remove legacy reflection backend.
11. Validate Android 15/16 API compatibility.
12. Add automated instrumentation tests for task lifecycle behavior.
