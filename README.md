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

- `MANIFEST.md` — all repos used
- `CHANGES.md` — every modification made
- `homelauncher-uncommitted.diff` — latest uncommitted launcher patches
- `homelauncher-git-log.txt` — full HomeLauncher git history
- `local_manifests/` — XML to add I001D device repos

## VM

- **Project:** `customrom-501702`
- **Zone:** `us-south1-b`
- **Instance:** `instance-20260707-045005`
- **User:** `premanandal1978`
- **ROM root:** `~/android/bliss-I001D`
- **Launcher path:** `~/android/bliss-I001D/packages/apps/HomeLauncher`
