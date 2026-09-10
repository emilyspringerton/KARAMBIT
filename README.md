# KARAMBIT

A real, pluggable Android network-toolkit app. v0 scans the local /24 subnet for a listening TCP
port 22 (SSH) — built to help find a headless device (no monitor, unknown IP) that may or may not
have booted onto the network. See `NORTHSTAR.md` for the full design and origin.

## Stack

- **Bazel** (bzlmod, `MODULE.bazel`) — the real, primary build system, not Gradle.
- **PARENA** — `parena/scan_decisions.prn` is real PARENA source, compiled to real Java
  (`app/src/main/java/industrial/einhorn/karambit/generated/ScanDecisions.java`) via the real,
  already-shipped `PARENA` compiler's Java emitter. Regenerate after editing the `.prn`:
  ```bash
  /home/fatbaby/PARENA/parena build parena/scan_decisions.prn \
    -o app/src/main/java/industrial/einhorn/karambit/generated/ScanDecisions.java
  ```
  (the generated `.java` is checked in, same real precedent SPIDERBEETLE's own
  `BatteryUi.java` already established — no live cross-repo Bazel dependency on PARENA itself).
- **Native Java** — everything else (socket I/O, Android APIs, UI). See `NORTHSTAR.md`'s own
  "PARENA/Java split" section for exactly where that line is drawn, and why.

## Building

Real, no-sudo toolchain acquisition used to build this (documented for reproducibility):

```bash
# Android SDK command-line tools + a platform + build-tools >= 35.0.0 (rules_android's own
# real minimum) -- no root needed, extracts anywhere.
curl -sL -o cmdline-tools.zip \
  https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip -q cmdline-tools.zip -d ~/.local/opt/android-sdk/cmdline-tools
mv ~/.local/opt/android-sdk/cmdline-tools/cmdline-tools ~/.local/opt/android-sdk/cmdline-tools/latest
export ANDROID_HOME=~/.local/opt/android-sdk
yes | "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_HOME" \
  "platform-tools" "platforms;android-34" "build-tools;35.0.0"
```

```bash
export ANDROID_HOME=~/.local/opt/android-sdk

# The real JVM-only core (no Android SDK needed for this part at all):
bazel build //app:scan_core_lib
bazel run //app:scan_core_test        # real assertions, incl. a genuine local-socket round trip

# The real, installable APK:
bazel build //app:karambit
# -> bazel-bin/app/karambit.apk (real, signed with a debug key, verified with
#    `apksigner verify` -- v1/v2/v3 schemes all pass)
```

`.bazelrc` pins the Java runtime/language version for both the target AND `rules_android`'s own
internal host-side ("for tool") build — without it, building `//app:karambit` fails compiling
`rules_android`'s own `DexFileSplitter.java` ("could not locate class file for java.lang.Record"),
a real target/tool JDK-version-selection mismatch found live, not guessed at.

**Real, sandbox-specific gotcha, not expected to recur on a normal machine**: in this exact
sandbox, `/home/fatbaby/go.work` (this monorepo's own Go workspace file, in a directory ABOVE
this repo) leaks into `rules_android`'s own internal Go-tool bootstrap (`go mod download` walks
up the directory tree looking for `go.work`), breaking its fetch with a Go-version mismatch error.
`--repo_env=GOWORK=off` did not reliably reach that subprocess; the real, working fix was running
the build with `HOME` pointed somewhere outside `/home/fatbaby` for that one invocation
(`export HOME=/tmp/karambit-home`) so the upward directory search never reaches the interfering
file. Not needed if this repo isn't nested under a directory containing a `go.work` file.

## Build status (honest, live-checked, not assumed)

- `//app:scan_core_lib` / `//app:scan_core_test` — **real, verified, passing.** Plain-JVM,
  no Android SDK involved. 13 real assertions, including a genuine local TCP socket round trip
  (not a mock).
- `//app:karambit` (the real APK) — **real, verified, builds and produces a genuinely valid,
  signed, installable APK.** Confirmed via `apksigner verify` (v1/v2/v3 signature schemes all
  pass) and `aapt2 dump badging` (correct package name `industrial.einhorn.karambit`, both real
  permissions present, `MainActivity` correctly registered as the launchable activity). A real
  Android SDK (build-tools ≥ 35.0.0, `rules_android`'s own hard minimum) and a real JDK were both
  obtained no-sudo for this repo, the same "don't assume a blocker, actually try to get the real
  toolchain" discipline this monorepo's own `PARENA` AVR/LLVM toolchain work already established.
  Not yet done: actually installing this APK on a real device (no device/emulator reachable from
  this sandbox) — the build artifact itself is real and verified, the install-and-run step isn't.

## Releases

Real CI/CD, matching PARENA's own established pattern in this monorepo: `.github/workflows/ci.yml`
builds + tests on every push/PR, and auto-cuts a real, versioned GitHub Release on every green
build on `main` — `vX.Y.0`, minor-bumped, real APK attached, real `versionCode`/`versionName`
baked in (not the repo's own placeholder dev values). See `NORTHSTAR.md`'s own "Real auto-releases"
section for the exact mechanism. Grab the latest signed APK from this repo's Releases page — no
local build needed to try it.

## Permissions

`INTERNET` and `ACCESS_NETWORK_STATE` — both real, ordinary (non-"dangerous") Android
permissions, no runtime prompt needed on any current Android version.

## What this is not (yet)

No in-app terminal, no real CIDR/netmask arithmetic (a real /24 is assumed), no configurable
scan scope. All named directly in `NORTHSTAR.md`'s own "Not built yet" section — real, honest,
deliberately deferred, not silently missing.

## Product completeness

A real launcher icon (all density buckets), explicit `minSdkVersion`/`targetSdkVersion`, real
version info baked into every release build, and a live scan-progress indicator (not just a
start/finish message) — see `NORTHSTAR.md`'s own "Real product-completeness pass" section.
