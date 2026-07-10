# Bliss Android 16 / ASUS ROG Phone II (I001D) Findings

Last updated: 2026-07-10

## Objective

Determine what is required to move the existing ASUS ROG Phone II (`I001D`) Bliss build from Android 14 to Bliss 19 / Android 16 (Waterlily). Use the latest official I001D OTA as the binary reference, unpack its partitions, compare device-specific contents against the current VM source and output, locate corresponding source repositories where possible, and document missing or changed device support.

## Scope and Safety

- Current source baseline: `/home/premanandal1978/android/bliss-I001D`
- Analysis workspace: `/home/premanandal1978/android16-i001d-analysis`
- The downloaded OTA and unpacked files stay outside the current ROM checkout.
- This task is analysis only. Do not modify, sync, or build the Android 16 source tree until the gap report identifies a viable source set.

## Confirmed Reference Build

- ROM: Bliss 19.6 Waterlily
- Android generation: Android 16
- Device: ASUS ROG Phone II (`I001D`)
- Variant: official vanilla device build, not a GSI
- Filename: `Bliss-v19.6-I001D-OFFICIAL-vanilla-20260616.zip`
- Published build date encoded in filename: 2026-06-16
- Official download page: <https://sourceforge.net/projects/blissroms/files/Waterlily/I001D/Bliss-v19.6-I001D-OFFICIAL-vanilla-20260616.zip/download>
- VM destination: `/home/premanandal1978/android16-i001d-analysis/downloads/Bliss-v19.6-I001D-OFFICIAL-vanilla-20260616.zip`
- Download status: complete and ZIP-validated
- Size: `1,522,868,957` bytes
- SHA-256: `5002294054f7b35d74961e21c77636c4cb28a1314d48fc6341e10d70c07743e0`

Bliss Waterlily source manifest entry point:

```text
repo init -u https://github.com/BlissRoms/stable_releases.git -b waterlily --git-lfs
```

Source reference: <https://github.com/BlissRoms/stable_releases>

## VM Capacity and Tools

- VM filesystem before extraction: 484 GB total, 293 GB used, 192 GB available (61% used)
- VM filesystem after both OTAs/images/filesystems were extracted: 313 GB used, 171 GB available (65% used)
- OS: Ubuntu 24.04.4 LTS (matches Bliss recommendation)
- CPU: 12 logical processors (meets the hexa-core-or-better recommendation)
- RAM: 47 GiB total, about 45 GiB available during inventory; no swap
- Java: OpenJDK 21.0.11 (matches requirement)
- Available tools: `unzip`, `python3`
- Not found in `PATH` during initial inventory: `lpunpack`, `simg2img`, `payload-dumper-go`
- Extraction tooling must be located in the Android checkout or installed/built in the isolated analysis workspace.
- The Waterlily README recommends 64 GB RAM and 500 GB or more free storage. This VM is below both recommendations, most critically storage: only 192 GB is free. A separate full Waterlily sync/build tree should not be started without expanding the disk or deliberately reclaiming space. No existing tree should be deleted as part of this analysis.

## Current Android 14 Baseline

Current built system metadata:

```text
ro.build.id=AP2A.240905.003
ro.build.version.release=14
ro.build.version.security_patch=2024-12-01
```

Existing source trees and checked-out revisions:

| Path | Revision | Files |
|---|---|---:|
| `device/asus/I001D` | `66473d931666769a627e65c6ee24b7f05fa61425` | 71 |
| `device/asus/sm8150-common` | `6cae628f8f24c75eac1d2a1759ac31df6dc25d77` | 216 |
| `vendor/asus/I001D` | `90c9d99e15b4df3e81fbba89ecdccfb29d5f98ba` | 943 |
| `vendor/asus/sm8150-common` | `b5f485169923f41bf3588df159e1351f5f4edb1b` | 908 |
| `kernel/asus/I001D` | `368dd4099045c66ae294f4a9d3717d615920c329` | 70,530 |

The current checkout already separates support into:

- Device-specific `I001D` configuration
- Shared Qualcomm `sm8150-common` configuration and HAL implementations
- Device-specific and shared proprietary vendor blobs
- ASUS `I001D` kernel source

Repository remotes:

| Path | Public remote |
|---|---|
| `device/asus/I001D` | <https://github.com/BlissRoms-Devices/android_device_asus_I001D> |
| `device/asus/sm8150-common` | <https://github.com/BlissRoms-Devices/android_device_asus_sm8150-common> |
| `vendor/asus/I001D` | <https://github.com/BlissRoms-Devices/proprietary_vendor_asus_I001D> |
| `vendor/asus/sm8150-common` | <https://github.com/BlissRoms-Devices/proprietary_vendor_asus_sm8150-common> |
| `kernel/asus/I001D` | <https://github.com/BlissRoms-Devices/android_kernel_asus_I001D> |

## Known Public Source State

- Bliss 19 Waterlily is based on Android 16; Bliss 19.3 was rebased onto Android 16 QPR2.
- The official Waterlily manifest is publicly referenced through `BlissRoms/stable_releases`, branch `waterlily` (current head observed and cloned on the VM: `e0b717c9a37b37fb35125e454df92887a419ebdf`).
- Manifest location on VM: `/home/premanandal1978/android16-i001d-analysis/sources/stable_releases`.
- The manifest pins AOSP `android-16.0.0_r4`, uses LineageOS `lineage-23.2` as its default upstream, and uses Bliss `waterlily-qpr2` projects.
- The manifest contains Android 16 / Lineage 23.2 Qualcomm SM8150 platform dependencies, including CAF audio, display, media, SM8150 display, and SM8150 GPS. Therefore the common Qualcomm platform source is available publicly; the main source gap is the ASUS device integration.
- The ordinary public LineageOS device index only lists historical I001D support (LineageOS 17.1 / Android 10). No current I001D Android 16 device branch was confirmed through public indexing.
- The public BlissRoms-Devices repositories for `I001D`, `sm8150-common`, both vendor trees, and the kernel have no `voyager`, `waterlily`, or Android 16 branch. Their newest public device-generation branch is `universe` (Android 14), exactly matching the VM checkout revisions.
- The official Bliss 19.6 I001D binary proves that a working or releasable Android 16 device source set exists, but the Android 16 device changes are not present in the obvious public device repositories. Possible explanations are private/unpushed branches, local manifest forks, or reuse of Universe trees with unpublicized patches.

Public branch heads observed:

| Repository | Newest relevant public branch/head |
|---|---|
| `android_device_asus_I001D` | `universe` at `66473d931666769a627e65c6ee24b7f05fa61425` |
| `android_device_asus_sm8150-common` | `universe` at `6cae628f8f24c75eac1d2a1759ac31df6dc25d77` |
| `proprietary_vendor_asus_I001D` | `universe` at `90c9d99e15b4df3e81fbba89ecdccfb29d5f98ba` |
| `proprietary_vendor_asus_sm8150-common` | `universe` at `b5f485169923f41bf3588df159e1351f5f4edb1b` |
| `android_kernel_asus_I001D` | `universe` at `368dd4099045c66ae294f4a9d3717d615920c329` |

The official OTA's embedded build manifest resolves the identity of the Android 16 device source. It was built from six repositories under `StudioKeys-Dumps`, all on `waterlily-qpr2`:

| Android 16 path | Repository | Exact revision |
|---|---|---|
| `device/asus/I001D` | `StudioKeys-Dumps/device_asus_I001D` | `00f97fddf6c039dbb410080bfa86073ed8971a77` |
| `device/asus/sm8150-common` | `StudioKeys-Dumps/device_asus_sm8150-common` | `5210b4084ef016443cebfb76819ea9796089997f` |
| `hardware/asus` | `StudioKeys-Dumps/hardware_asus` | `b284537e1b9aec88307711ea7a9a122cca4e1093` |
| `kernel/asus/I001D` | `StudioKeys-Dumps/kernel_asus_I001D` | `a8bb7fa3b185f3b69f46866aec55a119640c8112` |
| `vendor/asus/I001D` | `StudioKeys-Dumps/vendor_asus_I001D` | `b9a10ce1914d840ec0d70c3524c46954d3d6a280` |
| `vendor/asus/sm8150-common` | `StudioKeys-Dumps/vendor_asus_sm8150-common` | `f1679857ceaae10cf23eea4cfcf1a9e73b884898` |

These repositories are not publicly accessible at the time of analysis. Anonymous HTTPS requests prompt for credentials, they are absent from the organization's public GitHub API listing, exact commit searches return no public results, and the VM has no GitHub SSH key authorized for them. This is the primary blocker to a reproducible Android 16 build. Access should be requested using the exact repository names and commit IDs above.

The new `hardware/asus` project is a source dependency that does not exist in the Android 14 I001D manifest and must be included in any Waterlily local manifest.

Minimal I001D local-manifest content recovered from the official build:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <project name="StudioKeys-Dumps/device_asus_I001D"
           path="device/asus/I001D" remote="githubssh"
           revision="00f97fddf6c039dbb410080bfa86073ed8971a77" />
  <project name="StudioKeys-Dumps/device_asus_sm8150-common"
           path="device/asus/sm8150-common" remote="githubssh"
           revision="5210b4084ef016443cebfb76819ea9796089997f" />
  <project name="StudioKeys-Dumps/hardware_asus"
           path="hardware/asus" remote="githubssh"
           revision="b284537e1b9aec88307711ea7a9a122cca4e1093" />
  <project name="StudioKeys-Dumps/kernel_asus_I001D"
           path="kernel/asus/I001D" remote="githubssh"
           revision="a8bb7fa3b185f3b69f46866aec55a119640c8112" />
  <project name="StudioKeys-Dumps/vendor_asus_I001D"
           path="vendor/asus/I001D" remote="githubssh"
           revision="b9a10ce1914d840ec0d70c3524c46954d3d6a280" />
  <project name="StudioKeys-Dumps/vendor_asus_sm8150-common"
           path="vendor/asus/sm8150-common" remote="githubssh"
           revision="f1679857ceaae10cf23eea4cfcf1a9e73b884898" />
</manifest>
```

The `githubssh` remote is already defined by Waterlily's default manifest. Syncing this fragment requires a GitHub account/SSH key authorized for all six private repositories.

Key Waterlily SM8150 platform revisions:

| Path | Revision / branch |
|---|---|
| `hardware/qcom-caf/sm8150/audio` | `d1ae1d822e49422a2a02e9864578fb92491b355e` / `lineage-23.2-caf-sm8150` |
| `hardware/qcom-caf/sm8150/display` | `80826de19407b9bd7f67a24d744fa2c2f0cdda66` / `lineage-23.2-caf-sm8150` |
| `hardware/qcom-caf/sm8150/media` | `67641d710ec876fa922fcd10f6f65f7632e5ca08` / `lineage-23.2-caf-sm8150` |
| `hardware/qcom/sm8150/display` | `c13de989eb36bccdfaefb31c0071852e8a497ab4` / `lineage-23.2` |
| `hardware/qcom/sm8150/gps` | `c102ecaf661265a5bb4bab028c4dba8454016e1b` / `lineage-23.2` |

## Comparison Plan

1. Finish the official OTA download and calculate hashes.
2. Validate the ZIP and inventory top-level OTA entries.
3. Read `META-INF` metadata, compatibility data, dynamic partition information, and payload properties.
4. Extract `payload.bin` partitions with a reproducible tool in the isolated workspace.
5. Mount or extract filesystem images read-only.
6. Build normalized manifests for Android 16 and current Android 14 output:
   - file paths, modes, owners, SELinux labels where available
   - build properties and fingerprints
   - VINTF manifests and compatibility matrices
   - init scripts, fstab, ueventd, permissions, sysconfig, overlays
   - HAL services, HIDL/AIDL declarations, binaries, and shared-library dependencies
   - firmware, kernel modules, vendor blobs, and partition layout
7. Compare source-side device, common, vendor, kernel, and sepolicy trees.
8. Classify every difference as required, likely required, generated/common platform change, proprietary-only, or uncertain.
9. Produce an Android 16 bring-up requirement list and a recommended source migration sequence.

## Extracted OTA Facts

Android 16 OTA metadata:

```text
ota-type=AB
pre-device=WW_I001D,I001D,ZS660KL
post-sdk-level=36
post-security-patch-level=2026-06-01
post-build-incremental=378
```

The OTA uses a stock ASUS Android 11 compatibility fingerprint:

```text
asus/WW_I001D/ASUS_I001_1:11/RKQ1.200710.002/18.0210.2201.215-0:user/release-keys
```

This fingerprint is intentionally decoupled from the actual platform. Actual image properties confirm Android 16, SDK 36.1, build ID `BP4A.251205.006`, and June 2026 security patches.

Both Android 14 and Android 16 OTAs contain the same five A/B payload partitions and nominal sizes:

| Partition | Nominal size |
|---|---:|
| `boot` | 101 MB |
| `dtbo` | 8.4 MB |
| `system` | 3.8 GB |
| `vbmeta` | 4.1 KB |
| `vendor` | 1.1 GB |

Extracted Android 16 filesystem counts:

- System image: 7,163 files
- Vendor image: 2,527 files

Android 14 comparison counts:

- System image: 6,984 files
- Vendor image: 2,640 files
- Android 16 system path delta: 842 added, 663 removed
- Android 16 vendor path delta: 191 added, 304 removed
- Among common vendor paths, 801 files changed and approximately 1,535 remained byte-identical

The large unchanged vendor set indicates continued reuse of the old Qualcomm/ASUS proprietary base. Most bring-up changes are open-source wrappers, service migrations, policy, and rebuilt compatibility components rather than wholesale replacement of proprietary firmware.

## Device-Specific Android 16 Differences

Confirmed service/interface migrations:

| Android 14 | Android 16 |
|---|---|
| HIDL boot `android.hardware.boot@1.1-service` | AIDL `android.hardware.boot-service.qti` |
| HIDL camera provider `android.hardware.camera.provider@2.4-service_64` | `android.hardware.camera.provider-service.lineage` |
| No gadget AIDL service | `android.hardware.usb.gadget-service.qti` |
| HIDL memtrack `android.hardware.memtrack@1.0-service` | QTI memtrack service (`vendor.qti.hardware.memtrack-service`) |
| HIDL Lineage touch 1.0 service | AIDL `vendor.lineage.touch-service.asus_msmnile` |
| Device HIDL vibrator 1.2 service | QTI vibrator service plus `init.vibrator.rc` |
| Audio HAL implementation 6.0 | Audio HAL implementations 7.0/7.1 and soundtrigger 2.3 |
| `gralloc.msmnile.so`, `hwcomposer.msmnile.so` | `gralloc.qcom.so`, `hwcomposer.qcom.so` |
| ConfigStore HIDL service | `disable_configstore` migration helper; service removed |

The device VINTF manifest format moves from `8.0` to `9.0`, and framework compatibility target level moves from 4 to 6. The main manifest also updates:

- Audio devices factory: HIDL 6.0 to 7.1
- Audio effects factory: HIDL 6.0 to 7.0
- SoundTrigger: HIDL 2.1 to 2.3
- Tether offload control: HIDL 1.0 to 1.1
- Legacy camera provider, ConfigStore, memtrack, and irrelevant common-tree ASUS motor declarations are removed from the monolithic manifest

New VINTF fragments explicitly declare AIDL boot control, camera provider, USB gadget, memtrack, glove mode, and touchscreen gesture services.

Additional confirmed changes:

- New feature declaration: `asus.software.zenui.rog` via `/vendor/etc/permissions/asus-rog-features.xml`.
- Frozen board API and vendor SELinux policy version move from `202404` to `202504`.
- `ro.product.vendor.device` and system/product device identity change from `I001D` to `ZS660KL`, while OTA compatibility still accepts all three device identifiers.
- `vendor.usb.use_gadget_hal` changes from `0` to `1`.
- New `vendor.camera.skip_failed_ids=true` property.
- New product debugfs restriction property.
- The generated vendor VINTF manifest, compatibility matrix, SELinux policy, file contexts, property contexts, and service contexts all changed.
- Android 14 `AsusParts` (`org.blissroms.settings.asusparts`) is replaced by Android 16 `ROGParts` (`org.blissroms.settings.rogparts`, SDK/target 36), with new product/vendor overlays and privileged permissions.
- The stock ASUS Camera package is added as a privileged system extension app: `com.asus.camera`, version `v6.6.1.0_211022`, originating from the Android 11-era ASUS software base.
- Several old 32-bit ASUS camera/motor libraries are absent while their required 64-bit variants remain, indicating deliberate blob pruning rather than a complete vendor refresh.

Kernel/boot changes:

| Item | Android 14 baseline | Android 16 official |
|---|---|---|
| Kernel | Linux `4.14.190-omni+` | Linux `4.14.357-openela-perf+` |
| Compiler | Android Clang 12.0.7 | Android Clang 21.0.0 with PGO/BOLT/LTO/MLGO |
| Kernel image size | 23,053,852 bytes | 23,315,309 bytes |
| Ramdisk size | 14,625,824 bytes | 15,661,921 bytes |
| Kernel source revision | `368dd409...` public Universe | `a8bb7fa3...` private Waterlily QPR2 |

The DTBO hash also changed. The Android 16 boot command line adds `androidboot.boot_devices=soc/1d84000.ufshc` and removes the Android 14 `buildvariant=userdebug` argument.

Vendor kernel modules `br_netfilter.ko`, `lcd.ko`, and `msm-geni-ir.ko` are no longer shipped as modules in Android 16. Fourteen retained ASUS/accessory/storage modules were rebuilt against the new kernel.

Both embedded kernel configurations were recovered. The config diff contains 812 lines (55 enabled-value additions and 59 enabled-value removals, plus disabled-symbol/context changes). Notable Android 16 changes include:

- Forced kernel module signature verification (`CONFIG_MODULE_SIG_FORCE=y`)
- Newer BPF/JIT defaults and `CONFIG_NET_ACT_BPF=y`
- `CONFIG_USERFAULTFD=y`, jump labels, and task-trace RCU support
- Removal of bridge netfilter as a module, matching removal of `br_netfilter.ko`
- Removal of LCD class module support, matching removal of `lcd.ko`
- Debug write access and several Qualcomm/IOMMU debug options disabled
- ChaCha20/Poly1305 and additional DRBG crypto support enabled

The exact kernel config reports are preserved on the VM and should be treated as required input when validating or rebuilding the private Waterlily kernel tree.

First-stage mount/encryption changes:

- `system`, `metadata`, and `vbmeta` are explicitly first-stage mounted in Android 16.
- Device paths for system/metadata use `/dev/block/by-name/...`.
- `batinfo` receives an explicit mount entry.
- The Android 16 fstab is copied into `first_stage_ramdisk/system/etc/fstab.qcom`.
- `/data` encryption options change from `v2+inlinecrypt_optimized+wrappedkey_v0` with wrapped-key metadata encryption to plain `v2`/AES-XTS metadata encryption, and add F2FS checkpoint/sysfs settings.
- This encryption-format change is a high-risk upgrade item and likely makes clean-flash expectations important. It must be validated before proposing an in-place Android 14 to Android 16 upgrade.
- Recovery adds the AIDL QTI boot service and filesystem maintenance tools required by the revised layout.

## Analysis Artifacts on VM

Root: `/home/premanandal1978/android16-i001d-analysis`

Important paths:

- Official ZIP: `downloads/Bliss-v19.6-I001D-OFFICIAL-vanilla-20260616.zip`
- Waterlily manifest clone: `sources/stable_releases`
- Verified extractor: `tools/payload-dumper-go-1.3.0/payload-dumper-go`
- Android 16 OTA/payload: `unpacked/android16-ota`
- Android 16 raw images: `unpacked/android16-images`
- Android 16 extracted files: `unpacked/android16-files`
- Android 14 OTA/payload: `unpacked/android14-ota`
- Android 14 raw images: `unpacked/android14-images`
- Android 14 extracted files: `unpacked/android14-files`
- Generated inventories/diffs: `reports`

Key generated reports:

- `android14-system-paths.txt`, `android16-system-paths.txt`
- `android14-vendor-paths.txt`, `android16-vendor-paths.txt`
- `system-added-in-android16.txt`, `system-removed-in-android16.txt`
- `vendor-added-in-android16.txt`, `vendor-removed-in-android16.txt`
- `android14-vendor-sha256.txt`, `android16-vendor-sha256.txt`
- `vendor-common-paths-changed.txt`
- `android14-ramdisk-paths.txt`, `android16-ramdisk-paths.txt`
- `vendor-manifest-android14-to-16.diff`
- `fstab-android14-to-16.diff`
- `init-target-android14-to-16.diff`
- `android14-kernel-config.txt`, `android16-kernel-config.txt`
- `kernel-config-android14-to-16.diff`
- `android16-build-manifest.xml`
- `android16-I001D-projects.xml.fragment`
- `android16-ota-metadata.txt`
- `android16-vendor-manifest.xml`

## What Is Missing on the VM

The existing Android 14 checkout does **not** contain the following Android 16 requirements:

1. A full Waterlily platform checkout based on the pinned manifest (`android-16.0.0_r4`, Lineage 23.2, Bliss Waterlily QPR2).
2. Authorized access to the six private `StudioKeys-Dumps` repositories.
3. The new `hardware/asus` tree.
4. Android 16 revisions of the I001D device tree, SM8150 common tree, both vendor trees, and kernel tree.
5. The Android 16 kernel source/config/toolchain integration (OpenELA 4.14.357, Clang 21, forced module signatures).
6. The device-side HIDL-to-AIDL migrations, VINTF target-level 6 declarations, SELinux 202504 policy, first-stage fstab, ROGParts, ASUS Camera packaging, and updated overlays described above.
7. Enough free disk for a separate source checkout and build output. The VM currently has 171 GB free; Bliss recommends at least 500 GB free.
8. Recommended build memory. The VM has 47 GiB RAM and no swap; Bliss recommends 64 GB RAM.

The base Android 16 SM8150 CAF/platform dependencies are not missing conceptually: they are already pinned in the public Waterlily manifest and will arrive with a successful platform sync.

## Build Requirements and Recommended Sequence

Do not overwrite the working Android 14 tree. A defensible bring-up sequence is:

1. Obtain GitHub read access to the six exact private repositories/revisions.
2. Expand VM storage so at least 500 GB is free after retaining the Android 14 tree and this analysis. A root disk around 850 GB total is a practical minimum given current usage; more is safer for parallel outputs and ccache.
3. Increase memory to 64 GB or more. If machine resizing is not possible, add substantial swap and expect slower builds; this is below the official recommendation, not equivalent capacity.
4. Create a separate Waterlily checkout, for example `~/android/bliss-I001D-android16`.
5. Initialize using the official manifest:

   ```bash
   repo init -u https://github.com/BlissRoms/stable_releases.git -b waterlily --git-lfs
   ```

6. Add the recovered six-project local manifest and confirm SSH access before starting the full sync.
7. Sync the pinned platform/device revisions. Preserve the official build manifest as the reproducibility reference.
8. Verify the private source reproduces the extracted requirements before building:
   - VINTF manifest 9.0 / target level 6
   - SELinux/vendor API 202504
   - first-stage fstab and encryption settings
   - AIDL boot, camera, USB gadget, memtrack, and touch services
   - OpenELA 4.14.357 kernel/config and signed modules
   - ROGParts, ASUS Camera, ROG overlays/features
9. Run source-level compatibility checks (`checkvintf`, SELinux policy build, kernel/module checks) before generating a flashable OTA.
10. Build the official-equivalent vanilla target with Bliss's documented command:

    ```bash
    blissify I001D
    ```

11. Compare the resulting partition manifests, kernel config, and OTA metadata against the official 19.6 references in this analysis.
12. Treat first device testing as a clean-flash bring-up unless the encryption migration is independently proven safe. Back up persist/EFS-sensitive data and retain a tested rollback path.

Primary source entry points:

- Bliss Waterlily manifest: <https://github.com/BlissRoms/stable_releases/tree/waterlily>
- AOSP Android 16 tag used by the manifest: <https://android.googlesource.com/platform/manifest/+/refs/tags/android-16.0.0_r4>
- LineageOS Android source: <https://github.com/LineageOS/android>
- Bliss build support: <https://t.me/Team_Bliss_Build_Support>

## Remaining Runtime Validation

Binary/source comparison cannot prove runtime quality. These areas still require testing on a spare or recoverable I001D after source access and a reproducible build are secured:

- Boot, AVB, A/B slot switching, recovery, OTA installation, and rollback
- Decryption/format behavior and whether an in-place upgrade is possible
- Radio, SIM, mobile data, IMS, VoLTE/VoWiFi, emergency calling, and SAR behavior
- Camera sensors/modes/video, including the bundled ASUS Camera package
- Audio paths, microphones, speaker stereo, Bluetooth codecs, and SoundTrigger
- Fingerprint, keystore/keymaster, gatekeeper, and hardware-backed credentials
- Wi-Fi, hotspot, Bluetooth, NFC, GNSS, sensors, USB gadget/host, and charging
- 60/90/120 Hz display modes, brightness, color, and composition
- AirTriggers/ROG controls, glove mode, gestures, vibration effects, RGB/accessory docks, and external storage
- Suspend, thermal control, battery reporting, idle drain, and performance stability
- SELinux enforcing operation with no device-specific denials
- CTS/VTS-level compatibility appropriate for a custom ROM

## Current Status / Next Action

- Official Bliss 19.6 I001D OTA downloaded, hash-recorded, validated, and fully unpacked.
- Current Android 14 OTA unpacked through the same pipeline.
- Device source repositories and exact official build revisions recovered from the embedded manifest.
- Partition, vendor-path/hash, VINTF, fstab, ramdisk, boot, and kernel-config comparisons completed.
- Immediate blocker: obtain authorized access to the six private `StudioKeys-Dumps` repositories.
- Infrastructure blocker: expand VM disk and preferably RAM before a separate full Waterlily sync/build.
