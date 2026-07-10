# Modifications from stock BlissROM

## 1. BoardConfig kernel fix — `device/asus/sm8150-common/BoardConfigCommon.mk:130-132`

Commented out `TARGET_MODULE_ALIASES` for WLAN:

```makefile
# # Kernel modules - WLAN
# TARGET_MODULE_ALIASES += \
#     wlan.ko:qca_cld3_wlan.ko
```

**Why:** `CONFIG_QCA_CLD_WLAN=y` (built-in, not module). Build generates `mv $(find ... wlan.ko) ...` which fails → `mv: Needs 2 arguments`. Fixes `m otapackage`.

## 2. GApps variant — `vendor/bliss/config/versions.mk:16`

`BLISS_BUILD_VARIANT = gapps` must be set after `lunch` (file uses `?=`):

```bash
export BLISS_BUILD_VARIANT=gapps
```

## 3. Build patches (apply after `repo sync`)

| Patch | Applies to | Purpose |
|-------|-----------|---------|
| `android16/patches/I001D.patch` | `device/asus/I001D/` | Adds `COMMON_LUNCH_CHOICES` + `HomeLauncher` product package |
| `android16/patches/sm8150-common.patch` | `device/asus/sm8150-common/` | Removes Dolby dependency, fixes SELinux genfs contexts |
| `android16/patches/prebuilts-sdk.patch` | `prebuilts/sdk/` | Bumps minSdkVersion 19→21 in AndroidX library manifests |
| `android16/patches/HomeLauncher-vm-local.patch` | `packages/apps/HomeLauncher/` | VM-local launcher source changes |

## 4. HomeLauncher (`packages/apps/HomeLauncher`)

Source: `https://github.com/Prem8791/homelauncher`

### Uncommitted fixes (`homelauncher-uncommitted.diff`):

- **Recents tile XML** — overlay layout (label/close on snapshot), semi-transparent bar
- **Card aspect ratio** — `colWidth / displayRatio` eliminates letterboxing
- **Snapshot rotation** — removed `postRotate(orientation)` (orientation is 1/2, not degrees)
- **App drawer refresh** — `LauncherApps.Callback` reloads on package add/remove/change
- Removed `FIT_CENTER` runtime override, uses XML `centerCrop`
- **Letter strip screen nudge fix** — `GONE` → `INVISIBLE` when showing overlay, so centerColumn layout position stays stable

### Full git history in `homelauncher-git-log.txt`

## 5. Signing keys

`platform.pk8` + `platform.x509.pem` in project root for signing debug APKs.
