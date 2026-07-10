# BlissROM 14 (Android 14) — ASUS I001D (ZS630KL) — GApps Build

**BlissROM:** 17.8.3 Beta (Android 14.0, Universe)  
**Device:** ASUS Zenfone 6 (I001D / ZS630KL)  
**Build variant:** GApps  
**Lunch:** `bliss_I001D-ap2a-userdebug`

## Quick build

```bash
repo init -u https://github.com/BlissRoms/platform_manifests.git -b universe
repo sync -j$(nproc --all)
# copy local_manifests/ from this repo into .repo/local_manifests/
repo sync -j$(nproc --all)
source build/envsetup.sh
lunch bliss_I001D-ap2a-userdebug
export BLISS_BUILD_VARIANT=gapps
m otapackage
```

## Contents

- `MANIFEST.md` — all repos used (Android 14 BlissROM Universe)
- `CHANGES.md` — every modification made
- `homelauncher-uncommitted.diff` — latest uncommitted launcher patches
- `homelauncher-git-log.txt` — full HomeLauncher git history
- `local_manifests/` — I001D device repo XMLs (Android 14)
- `android16/` — **Android 16 (Waterlily) bring-up analysis:**
  - `findings.android16.md` — full gap analysis (VINTF, fstab, kernel, services, encryption)
  - `device/` — A16 VINTF manifests, init RC files, fstab, SELinux policy, ramdisk extracted from OTA
  - `vendor/asus/` — vendor blob inventory + SHA256 diffs (A14 vs A16)
  - `kernel/asus/I001D/` — kernel config + 812-line A14→A16 config diff
  - `patches/` — all `.diff` files plus critical build patches:
    - `I001D.patch` — adds `COMMON_LUNCH_CHOICES` and `HomeLauncher` product packages
    - `sm8150-common.patch` — removes Dolby, fixes SELinux genfs contexts
    - `prebuilts-sdk.patch` — bumps minSdkVersion 19→21 in AndroidX manifests
    - `HomeLauncher-vm-local.patch` — VM-local launcher changes
    - `fstab-android14-to-16.diff`, `vendor-manifest-android14-to-16.diff`,
      `kernel-config-android14-to-16.diff`, `init-target-android14-to-16.diff`
  - `bringup-i001d-to-waterlily.sh` — bring-up script
  - `extract-vendor.sh` — blob extraction instructions
  - `local_manifests/waterlily-i001d.xml` — 6 private `StudioKeys-Dumps` repo refs
  - `android14-configs/` — A14 BoardConfig, device.mk for reference

## VM

- **Project:** `customrom-501702`
- **Zone:** `us-south1-b`
- **Instance:** `instance-20260707-045005`
- **User:** `premanandal1978`
- **ROM root:** `~/android/bliss-I001D`
- **Launcher path:** `~/android/bliss-I001D/packages/apps/HomeLauncher`
