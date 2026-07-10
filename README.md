# Bliss I001D Build Backup

Device: ASUS I001D
ROM output: bliss_I001D-ota.zip

Important local fixes:
- Allows missing required modules for this device build setup.
- Disables unsupported face unlock.
- Disables the missing Dolby product inherit.
- Removes duplicate/problematic wakeup sepolicy entries.

Files:
- `manifest.xml`: original repo manifest snapshot
- `local_manifests/`: local device/vendor manifest setup
- `my-changes.patch`: local source edits
- `repo-status.txt`: changed repo status
- `build-sha256.txt`: SHA256 of generated OTA zip

Restore idea:
1. Sync Bliss source with the same branch/manifests.
2. Apply `my-changes.patch` from source root:
   patch -p1 < my-changes.patch
3. Build:
   source build/envsetup.sh
   lunch bliss_I001D-userdebug
   mka otapackage -j$(nproc)

Note:
- `hardware/dolby` in local manifest may require GitLab authentication.
