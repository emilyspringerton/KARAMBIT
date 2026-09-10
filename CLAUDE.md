# KARAMBIT

## What This Is

A real, pluggable Android network-toolkit app. v0 scans the local /24 subnet for a listening
TCP port 22 (SSH) — built to help find a headless device (no monitor, unknown IP) that may or
may not have booted onto the network. See `README.md` for the real, current build status and
`NORTHSTAR.md` for the full real scope/plan (what's shipped, what's honestly not attempted, and
why).

## Status

See `README.md`'s own "Build status" section for the current, live-checked state — kept there
rather than duplicated here since it changes as this repo builds out.

## Stack

Bazel (bzlmod), not Gradle — a real, explicit founder choice. `rules_android` for the real,
installable APK; `rules_java` for the plain-JVM-testable core
(`app/src/main/java/industrial/einhorn/karambit/{ScanEngine,ScanResult,ScanStrategy,
SshPortScanStrategy}.java` + PARENA-generated `generated/ScanDecisions.java`), independently
buildable/testable with no Android SDK/emulator involved at all.

PARENA owns the real, narrow decision logic that fits its own real language surface (see
`NORTHSTAR.md`'s "PARENA/Java split"); everything else — socket I/O, Android APIs, UI — is real,
hand-written Java.

## Related Repos

- `PARENA` — the language/compiler; `parena/scan_decisions.prn` is the real source this repo's
  own generated Java comes from. Regenerate via
  `PARENA/parena build parena/scan_decisions.prn -o app/.../generated/ScanDecisions.java`.
- `SPIDERBEETLE` — a different real Android app in this ecosystem using the exact same real
  "PARENA emits pure decision logic, a thin native host does real I/O" pattern (its own
  `battery_ui.prn`/`BatteryUi.java`) — the direct precedent this repo follows, though
  SPIDERBEETLE itself stays plain javac/java (no Bazel).
- `MJOLNIR` — this monorepo's own other real, existing Android app (Kotlin/Jetpack Compose).
- `EMILY` — RSI loop / backlog coordination for cross-repo work.

## Founder Real-Time Direction

Whenever the founder gives real-time direction — a new ask, a correction, a "can we also..." —
route it through `emily observe -s info "Founder real-time: <summary>"` first, even if it isn't
this repo's usual domain, then sprint-plan it into `EMILY/BACKLOG.md` (`emily backlog curate`,
scoped into a real SECTION/sub-item, not just a one-line log), and only then implement. See
`EMILY/docs/THE_EMILY_WAY.md` Principle 18 ("Pave the Cow Paths").

## Apple Filing Protocol

After any meaningful change, file an Apple:
```bash
emily apples post -t completion -repo KARAMBIT "<title>" "<body with commit hash>"
```
Then mark the item done in `EMILY/BACKLOG.md` and commit.

## CHANGELOG Protocol

After any meaningful change, update CHANGELOG.md:
```bash
emily changelog add KARAMBIT "<what changed>"
# or manually: append a dated bullet under ## YYYY-MM-DD in KARAMBIT/CHANGELOG.md
```

## Golden Doc Registration

If you create a new NORTHSTAR.md, architecture spec, or mission-critical design doc in this repo,
append a row to `EMILY/context/golden-docs-index.md` so Emily Prime picks it up on the next cycle.
Then commit and push EMILY.

## Frame-Break Reframing

Founder-sourced prompting technique (REDGARDEN/NORTHSTAR.md §28, full origin in
REDGARDEN/docs2/MULTI_AGENT_RD_RESEARCH_NOTES.md §5): given a request, name the underlying
structural/systemic pattern it's one instance of — one level of abstraction up — as an added
lens during planning/triage/judgment calls. Use it to spot the general case behind a specific
ask. It augments judgment, it does not replace doing the work: direct, concrete execution of
the literal task asked for still happens every time.

## Commit Protocol (standing instruction)

Always commit and push completed work immediately — don't wait to be asked. This is the default
for every repo in this monorepo.

Every commit — human-written or produced by automated code paths — must carry the active
`emily session` fingerprint as a `session: <tag>` trailer (blank line, then the trailer).
