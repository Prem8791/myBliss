# Deployment Package Manifest

## 1. Concise Architecture Summary

File:

```text
rom-integration/docs/architecture-summary.md
```

## 2. Every Required Source Patch

File:

```text
rom-integration/patches/0001-source-refactor-hidden-api-compat.patch
```

This patch contains the app-side refactor:

- removes direct `HiddenApi` usage
- introduces `RecentTasksBackend`
- introduces `RecentTasksRepository`
- moves reflection into `system.hiddenapi`
- fixes `startActivityFromRecents(int, Bundle)`
- adds TaskOrganizer backend placeholder
- removes Gradle-only manifest `tools:` annotations

## 3. Every Required Build System Patch

Files:

```text
rom-integration/aosp/Android.bp
rom-integration/patches/0002-add-aosp-home-launcher-module.patch
```

## 4. Every Required Product Configuration Patch

Files:

```text
rom-integration/product/home_launcher_product.mk
rom-integration/patches/0004-add-product-packages.patch
```

## 5. Every Required Privapp Allowlist

Files:

```text
rom-integration/aosp/permissions/privapp-permissions-com.home.launcher.xml
rom-integration/patches/0003-add-privapp-permissions.patch
```

## 6. Draft SELinux Policy Files

Files:

```text
rom-integration/sepolicy/draft/README.md
rom-integration/sepolicy/draft/platform_app_home_launcher.te
rom-integration/patches/0006-deferred-selinux-proc-thermal.patch
```

Status: deferred. Do not enable before first ROM-bundled boot and fresh AVC collection.

## 7. Exact Cloud VM Execution Checklist

File:

```text
rom-integration/docs/cloud-vm-execution-checklist.md
```

## 8. Post-Flash Verification Checklist

File:

```text
rom-integration/docs/post-flash-verification-checklist.md
```

## 9. Expected First-Boot Failures

File:

```text
rom-integration/docs/expected-first-boot-failures.md
```

## 10. Intentionally Deferred Tasks

File:

```text
rom-integration/docs/deferred-tasks.md
```

## Additional Supporting Audits

Files:

```text
rom-integration/docs/current-project-audit.md
rom-integration/docs/permission-audit.md
rom-integration/docs/taskorganizer-migration-roadmap.md
```

## Optional Future Overlay

Files:

```text
rom-integration/aosp/overlays/HomeLauncherConfigOverlay/
rom-integration/patches/0005-optional-recents-component-overlay.patch
```

Status: optional future work. Do not add to `PRODUCT_PACKAGES` until `com.home.launcher` implements the QuickStep/Overview service contract.
