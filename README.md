# Bliss I001D Build Backup

Device: ASUS I001D
ROM output: bliss_I001D-ota.zip

Important local fix:
- Removed/commented AOSP `webview` from `build/make/target/product/media_product.mk`
- This avoids build failure from missing/invalid `external/chromium-webview/prebuilt/arm64/webview.apk`
- GApps/Trichrome/WebView should provide WebView instead

Files:
- `manifest.xml`: original repo manifest snapshot
- `local_manifests/`: local device/vendor manifest setup
- `my-changes.patch`: local source edits, including WebView fix
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
