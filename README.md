# FutuLink

A network measurement Android app built as a test assignment. On first launch it asks a remote
JSON config which test to show, caches the answer, and from then on runs entirely from that cached
decision. The implemented test is a real streamed **Speed Test**.

## Stack

Kotlin · Jetpack Compose (Material 3) · Coroutines + Flow · Koin · Ktor · Preferences DataStore ·
Room · MVVM + Clean Architecture · single Gradle module · `minSdk 26`, `compileSdk`/`targetSdk 36`,
Java 17.

## Remote configuration

**URL** (the only place it is defined is the `REMOTE_CONFIG_URL` build config field in
[`app/build.gradle.kts`](app/build.gradle.kts)):

```
https://gist.githubusercontent.com/mykola-koshmanov/1cdf8d6a522ab06d32e48dcc42e5bb6a/raw/config.json
```

**Format**

```json
{
  "mode": "speed"
}
```

Supported values: `speed`, `ping`.

**Why a GitHub gist** — it gives a stable HTTPS-hosted JSON file with no backend, no accounts and
no deployment step, which keeps the focus on the client behaviour. A production app would use a
managed config service with versioning and staged rollout.

**Resolution rules**

| Response | Mode | Cached? |
| --- | --- | --- |
| `{"mode":"speed"}` / `{"mode":"  PiNg  "}` | `SPEED` / `PING` (trimmed, case-insensitive) | yes |
| unknown value, `null`, missing property, malformed body | `SPEED` (default) | yes |
| timeout, offline, non-2xx | — error screen with **Retry** | **no** |

The mode is stored in DataStore under `selected_test_mode`. Absence of that key is the only
"never initialised" signal, so later launches read the cache and never hit the network. To test a
different mode, change the gist and clear the app data (`adb shell pm clear com.futulink.android`).

## Speed test

**Endpoint**

```
https://fsn1-speed.hetzner.com/100MB.bin
```

The assignment's default URL (`speed.cloudflare.com/__down?bytes=200000000`) answers **HTTP 403**
for any request of 100,000,000 bytes or more, so it cannot produce a measurement. The assignment
allows your own source of at least 100 MB of non-compressible content; Hetzner's public file is
exactly **104,857,600 bytes** (`Content-Length`, confirmed by a mid-file range request) of random
data — a 64 KB sample uses all 256 byte values and *grows* under gzip — served as
`application/octet-stream` over HTTPS with no redirect and no cookies.

**How it works**

- **Streamed, never buffered.** The body is read through `ByteReadChannel` into one reused 64 KB
  array, so 100 MiB never sits in memory.
- **10 s window, sampled every 500 ms**, driven by `SystemClock.elapsedRealtimeNanos()` —
  wall-clock time can jump and would corrupt the durations.
- **Mbps** = `bytes × 8 / seconds / 1,000,000` (decimal, as providers quote it).
  **current** = bytes in the last interval over that interval's real length;
  **average** = bytes at the deadline over the measured window; **peak** = highest sample.
  Rounding happens only for display.
- **What is counted:** HTTP response body bytes only — no TCP/TLS or header overhead — so this is
  application payload throughput over a single connection, not a line-rate benchmark.
  `Accept-Encoding: identity` and `Cache-Control: no-cache` stop the server compressing or
  short-cutting that body.
- **Repeat requests.** The body is finite; if it ends before 10 s the next request starts
  immediately and the counter keeps accumulating.
- At the deadline the clock and counter are read **before** the download is cancelled, so cleanup
  time is not part of the measured duration.
- Zero bytes, or a failure before completion, fails the test instead of storing a result.

## Cancellation

Pure structured concurrency — no flags.

- **Stop** cancels the measurement `Job`. Because `cancel()` is asynchronous, the handle is kept
  and the next test `join()`s it first, so a fast Stop → Start cannot overlap two downloads.
- **Leaving the screen** cancels it: switching to Statistics from the bottom bar, and losing
  visibility (Home, app switcher, lock screen) via an `ON_STOP` observer. That observer ignores
  `Activity.isChangingConfigurations`, so a rotation does not cancel a running test.
- **ViewModel destruction** is the final guarantee via `viewModelScope`.
- The Ktor response is closed by `prepareGet(...).execute { }`, which releases it when the block
  returns, throws *or* is cancelled — verified on an emulator: interface traffic drops to 0 KiB
  after Stop, a tab switch and Home.
- `CancellationException` is always rethrown, never turned into an error and never saved.

## Architecture

One module, layered by package:

- **domain** — pure Kotlin: models, repository interfaces, the two use cases that carry real logic
  (`ResolveStartupModeUseCase` asks the cache before the network, `SaveMeasurementUseCase` builds
  the history record). Where a ViewModel only needs a stream it uses the repository interface
  directly rather than adding a file that forwards one call.
- **data** — Ktor, DataStore, Room, mappers. DTOs and entities never leave this layer.
- **presentation** — ViewModels expose immutable sealed UI state as read-only `StateFlow`;
  composables take state plus callbacks and never touch a repository.
- **di** — Koin modules.

Two details worth calling out: the config fetch starts in `StartupViewModel.init`, never from
composition, so a recomposition or rotation cannot trigger a second request; and error states
carry a `@StringRes Int` rather than a ready-made sentence, which keeps every user-facing string
in `strings.xml`.

Room stores measurements in a deliberately generic shape (`minimumValue` / `averageValue` /
`maximumValue` / `unit`) so a Ping test could reuse the table without a migration. `observeAll()`
returns a newest-first `Flow`, which is what makes Statistics update itself as soon as a test ends.

## Decisions and trade-offs

- **Speed Test** was chosen because it exercises streaming, `Flow`, live state and cancellation —
  the interesting parts of the brief.
- **Ping Test is a placeholder**, as the assignment allows: only the mode the submitted config
  points to needs to be real.
- **A gist instead of a backend** — smallest thing that makes the first-launch flow genuine.
- **OkHttp engine, not Ktor's Android engine.** The Android engine closes the response from the
  cancelling thread, and Android's bundled OkHttp then throws `Unbalanced enter/exit`; that
  escapes a coroutine completion handler and killed the process on every Stop. Reproduced on an
  emulator, fixed by the engine swap.
- Readability over abstraction: no generic base classes, no `Result` wrapper, typed domain
  exceptions instead.

## Not implemented

- **Ping Test** — by design; the submitted config selects `speed`, and the assignment requires only
  that one. Selecting `ping` shows a *Mode unavailable* screen.
- **Dark theme, localisation, tablet layouts** — explicitly out of scope. The app is light-theme
  and English; portrait and landscape both work.
- **Automated tests** — none are included, so none are claimed. The app was verified manually on an
  emulator: first launch online and offline, Retry, live Mbps, completion at ~10 s, one Room row
  per successful test, and no traffic after Stop / tab switch / Home / Back.

## Build

Requires JDK 17 and Android SDK platform 36.

```
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
Minimum supported version: **Android 8.0 (API 26)**.
