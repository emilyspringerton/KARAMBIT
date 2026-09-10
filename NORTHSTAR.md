# KARAMBIT — Northstar

## Origin

Founder real-time (2026-09-10): "im having a hard time figuring out if my raspberry pi is
booting and connecting to the network.. i dont have a monitor or anything to plug in i dont know
if it would respond to a network scan but i think it might... can we build a toolkit android app
to help scan my network to see if i can find it? ssh for example should be open... maybe we just
check for that for now so we aren't too noisy on the network... we dont need to avoid detection
but i figure our work will be available to security professionals in case it ends up being good.
for example if we wanted to do a more involved scan we can do that but for now i just want to
quickly check to see if there is a machine on the network with port 22 ready for action... keep
it pluggable and extensible we may very well want to build an in app terminal right there to
connect to it (not yet just sayin) build it parena native as much as possible interopping with
native java use BAZEL and PARENA the upstream repo is called KARAMBIT."

A real, legitimate home-network diagnostic tool — finding your own headless device on your own
LAN — not a covert or evasive scanning tool. The founder's own framing ("we dont need to avoid
detection") and expectation that "our work will be available to security professionals" is taken
at face value: this repo is written to be read, not hidden.

## What this is

An Android app that scans the local /24 subnet for a listening TCP port 22 (SSH) — the real,
narrow v0 the founder asked for, not a general-purpose port scanner (yet). Built with:

- **Bazel** (not Gradle) as the primary build system — a real, explicit founder choice, matching
  this same founder's own established PARENA-repo precedent ("continue PARENA build it with
  BAZEL"). `rules_android` builds the real, installable APK; `rules_java` builds/tests the
  plain-JVM-testable core independently of any Android SDK/emulator.
- **PARENA**, for the real decision logic that fits its own real, narrow language surface (see
  "PARENA/Java split" below) — compiled to real Java via PARENA's own already-shipped
  `emit_java.c` backend, the same real precedent SPIDERBEETLE's `stdlib/android/battery_ui.prn`
  already established for a different Android app in this ecosystem.
- **Native Java**, for everything PARENA's real language surface genuinely can't do yet: socket
  I/O, Android's own `ConnectivityManager`/UI APIs, thread pooling.

## PARENA/Java split — what's real decision logic vs. real native I/O

`parena/scan_decisions.prn` compiles to `app/.../generated/ScanDecisions.java` and owns:
- `is-target-port` — which port counts as "the target" (22, v0). A real, single, named decision
  point rather than a bare `22` scattered through Java — widening this to a real, pluggable
  multi-port scan later is a one-line PARENA change.
- `has-more-hosts` / `next-scan-index` — the real loop-control arithmetic `ScanEngine`'s host
  enumeration loop uses.
- `classify-scan-result` — the one real place a raw "did the socket connect" boolean becomes a
  stable status code.

Everything else — the actual `Socket` connect/close (`SshPortScanStrategy`), the bounded
thread-pool scan loop (`ScanEngine`), Android's own network-detection API calls (`NetworkInfo`),
and the UI (`MainActivity`) — is real, hand-written Java. PARENA's own real language surface has
no socket primitives and no bitwise integer operators yet (only `+ - * /`, comparisons, and
boolean `and`/`or`/`not` — see `PARENA/src/emit_llvm.c`'s own real, checked binop table), so real
CIDR/netmask bit arithmetic isn't attempted here either; see `ScanEngine.java`'s own header
comment for the honest, named consequence (a real, ordinary /24 assumption, not real netmask
math).

## Real, deliberate "not too noisy" design

The founder's own words: "maybe we just check for that for now so we arent too noisy on the
network." v0 checks exactly ONE port (22) with a small, fixed thread pool (8 concurrent probes,
not one thread per host, not all 254 hosts at once) and a short per-host timeout. This is a real,
considered default, not a placeholder — a knob to make it noisier is real, separate, deliberately
deferred scope (see "Not built yet" below), not something anyone should be able to accidentally
turn up today.

## Real, pluggable architecture

`ScanStrategy` is the seam: any real single host:port probe (today: a bare TCP connect) implements
it. `ScanEngine` depends only on the interface. Adding a second real probe kind (a banner grab, an
HTTP HEAD, a real SSH handshake attempt) is a new class, not a rewrite. `ScanResult` is a plain,
stable data shape designed so a later feature (see below) can act on it without rework.

## Real product-completeness pass (2026-09-10, same day)

Closed the gaps a real, shippable v0 needs beyond the core scan logic: a real launcher icon
(a generated radar-style PNG at every real density bucket, `mdpi` through `xxxhdpi`), explicit
`minSdkVersion`/`targetSdkVersion` in the manifest (previously left implicit and silently patched
by a Bazel build step — now documented, not a build-tool side effect), real `versionCode`/
`versionName` (see "Real auto-releases" below for how a real release gets a real version baked
in), and a live scan-progress indicator (`N/254 checked`, not just a start/finish message) —
KARAMBIT's own `MainActivity` now gives real, continuous feedback during a scan instead of going
quiet until the whole /24 finishes.

## Real auto-releases

Same real scheme PARENA's own `ci.yml` already established in this monorepo: every green build on
`main` auto-bumps the MINOR version (`vX.Y.0 -> vX.(Y+1).0`) and cuts a real GitHub Release with
the real APK attached, versioned for real (`versionCode` = `github.run_number`, a real,
monotonically-increasing integer across this repo's whole CI history; `versionName` = the
computed tag). `.github/workflows/ci.yml`'s own `build_and_test` job runs on every push/PR (real
plain-JVM tests + a real, placeholder-versioned APK build, uploaded as a downloadable workflow
artifact); the `release` job (main-branch pushes only) re-builds with the real version sed-patched
into `app/BUILD.bazel` first, so the shipped APK genuinely carries a real, meaningful version, not
a build-time constant.

## Not built yet (named directly, not hidden)

- **An in-app terminal** — the founder's own explicit "we may very well want to build an in app
  terminal right there to connect to it (not yet just sayin)." `ScanResult`'s own shape is
  designed with this in mind (host, port, open/closed), but no SSH client, no terminal UI, and no
  "tap a result to connect" action exist yet.
- **Real CIDR/netmask arithmetic** — v0 assumes an ordinary /24 network; a real, different subnet
  size scans the wrong host range. Needs either a real Java-side netmask computation or, for the
  PARENA-native route, first adding bitwise AND/OR/shift operators to PARENA's own language
  (`src/emit_llvm.c`'s real binop table) — a real, separate, not-yet-scoped compiler change.
- **A configurable scan scope** — port list, concurrency, timeout are all fixed constants in v0,
  deliberately (see "not too noisy" above).
- **Installing/running the built APK on a real device** — the APK itself is real, signed, and
  verified (`apksigner verify`, `aapt2 dump badging`), see `README.md`'s own "Build status"
  section — but no device/emulator is reachable from this sandbox to actually install and run it.
  A real GitHub Release APK (see "Real auto-releases" above) is the real path to actually testing
  this on a device once CI has run on `main`.
