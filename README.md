# Dragline

**Dragline** is an open-source Android wake-up and morning-routine app in development for people who ignore, snooze, or dismiss repeated alarms.

The goal is more than making an alarm sound: help the user become consciously awake, start their morning routine, and avoid falling asleep again. Dragline is being developed for personal use first, with broader device support and public release planned after that experience is useful.

**Current status: v0.1.0 skeleton.** The complete experience below is planned, not delivered. Keep a trusted backup alarm while evaluating the prototype; the current implementation does not establish reliable operation under every device condition.

The [approved roadmap](docs/ROADMAP.md) is the source of truth for product direction, the implementation audit, and the staged task breakdown.

---

## Product Direction

- **Music that stays varied:** Spotify playlists and varied built-in sounds are both required, with recent-track avoidance. Spotify remains a hard requirement, subject to the [approval and feasibility gate](#spotify-integration-status).
- **Deliberate wake-up tasks:** Choose or combine QR, NFC, step-count, and other approved tasks depending on the morning.
- **Morning follow-through:** Configurable routine checkpoints, timed wakefulness checks, and re-alerting when a check is missed. Routines can support activities such as putting on shoes, walking the dog, or making coffee.
- **Per-alarm control:** Configurable strictness and planning automation, rather than a universal no-snooze rule. Even the strictest alarm must retain a deliberate emergency stop distinct from ordinary snooze.
- **Context-aware timing:** Fixed-time, sunrise, and calendar-aware modes. Calendar planning should account for meeting location, preparation time, buffers, and public transit, initially around Seattle, Redmond, and Bellevue. Combined scheduling rules come later.
- **Reliability, privacy, and cost boundaries:** Explicit readiness and failure states, offline local fallback, minimal routing data, and external services that are free at personal usage levels.

These are product requirements, not claims about the current skeleton. A completed task or checkpoint is also not proof that someone is consciously awake; personal trials must evaluate actual morning outcomes.

---

## Current Capabilities (v0.1.0 Skeleton)

| Area | Implemented foundation |
| --- | --- |
| App shell | Material 3 / Compose destinations for Alarms, Sound Sources, and Settings, with theme preferences and an alarm editor. |
| Alarm configuration | Create, edit, enable, disable, and delete fixed-time alarms; select repeat days, labels, sounds, vibration, and gradual volume settings. |
| Scheduling | `AlarmManager.setAlarmClock` when exact scheduling is available; an inexact `setAndAllowWhileIdle` fallback otherwise or after a permission-related `SecurityException`. `BootReceiver` attempts restoration after reboot or app update. |
| Local audio | Two bundled tones (`beacon.wav` and `resonance.wav`), previews, foreground-service playback, volume ramping, and vibration. See the known sound-selection and playback gaps below. |
| Ringing UI | Lock-screen/full-screen notification wiring, a ticking clock, and a three-second **Hold to Wake** challenge. The current UI has no snooze button and exposes **Debug Dismiss** only in debug builds. |
| Provider and selection groundwork | `SoundProvider`, `LocalSoundProvider`, and `FakeSpotifyProvider`; a track-selection algorithm that avoids supplied recent IDs and uses least-recently-played selection when the pool is exhausted. This pipeline is not connected to ringing. |
| Persistence and preferences | Room storage for alarms and track history, plus local settings for theme, fallback sound, exclusion window, gradual-volume duration, and vibration. Persisting an exclusion preference does not yet enable music rotation. |

## Known Gaps and Limitations

The [source-level audit](docs/ROADMAP.md#2-current-implementation-audit) distinguishes implementation findings from behavior still requiring device evidence. In particular:

- **Spotify is mocked.** There is no live account connection or Spotify playback. Selecting a mock playlist currently makes the ringing service play local audio directly.
- **Primary/fallback audio is not correctly separated.** Local alarms currently play the configured fallback tone even when a different primary local sound is selected. Playback failure results are not acted on, and previews share the alarm's audio player.
- **Lifecycle reliability needs work.** Interrupted and overlapping alarms, recurrence after interruption, device-readiness changes, and database upgrades need stronger handling and device-level evidence.
- **Physical challenges and contextual schedules are placeholders.** QR, NFC, and step challenges are marked "Coming soon"; sunrise and meeting calculators use placeholder times. Location and calendar permissions are not requested by the skeleton.
- **Routine support and configurable strictness are planned.** Routine checkpoints, follow-up checks, per-alarm snooze/strictness policies, and an always-available release-build emergency stop are not delivered by the current Hold to Wake screen.

The existing unit tests cover fixed-time calculations, track selection, repository/mapper behavior with a fake DAO, and hold-duration logic. They do not establish end-to-end alarm reliability.

---

## Technical Stack

- **Language**: Kotlin 2.4.10
- **UI Framework**: Jetpack Compose with Material 3 & Navigation Compose
- **Architecture**: Pragmatic Layered Architecture (UI, Domain, Data, Alarm, Audio, Challenge, Scheduling)
- **Dependency Injection**: Dagger Hilt 2.60.1
- **Persistence**: Room 2.8.4 (with KSP 2.3.11) & SharedPreferences Flow
- **Build System**: Android Gradle Plugin 9.0.1, Gradle 9.7.1 (Kotlin DSL)
- **Target SDK**: 36 | **Minimum SDK**: 26 (Android 8.0+) | **Java**: 17

Versions are declared in the [version catalog](gradle/libs.versions.toml), [Gradle wrapper](gradle/wrapper/gradle-wrapper.properties), and [app build configuration](app/build.gradle.kts).

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the skeleton's subsystem layout and diagrams. Use the [approved roadmap](docs/ROADMAP.md) for current priorities, known discrepancies in the architecture document, and reliability acceptance criteria rather than treating the skeleton's descriptions as guarantees.

---

## Project Structure

```
app/src/main/java/com/mcasillas/dragline/
├── DraglineApp.kt                    # Application class with @HiltAndroidApp
├── MainActivity.kt                   # Single Activity hosting DraglineNavHost
├── data/
│   ├── local/
│   │   ├── DraglineDatabase.kt       # Room Database definition
│   │   ├── converters/AlarmMapper.kt # Entity <-> Domain mappers
│   │   ├── dao/                      # Room DAOs (AlarmDao, PlayedTrackDao)
│   │   └── entity/                   # Room entities (AlarmEntity, PlayedTrackEntity)
│   ├── preferences/                  # Local user settings persistence
│   └── repository/                   # Repository implementations
├── di/                               # Hilt dependency injection modules
├── domain/
│   ├── model/                        # Domain models (Alarm, Schedule, DayOfWeek, Track, etc.)
│   ├── repository/                   # Repository interfaces
│   └── usecase/                      # Core business use cases (SelectTrack, SaveAlarm, etc.)
├── alarm/
│   ├── AlarmScheduler.kt             # System AlarmManager scheduler abstraction
│   ├── AlarmReceiver.kt              # Scheduled trigger broadcast receiver
│   ├── BootReceiver.kt               # Reboot / package-update receiver
│   ├── AlarmNotificationManager.kt   # High-importance notifications & fullScreenIntent
│   └── service/AlarmRingingService.kt # Foreground audio, vibration, and wake lock service
├── audio/
│   ├── SoundProvider.kt              # Audio provider abstraction
│   ├── LocalSoundProvider.kt         # Bundled offline audio player
│   ├── FakeSpotifyProvider.kt        # Mock Spotify preview provider
│   ├── AudioPlayer.kt                # MediaPlayer wrapper with volume ramping
│   └── VibratorHelper.kt             # Waveform vibration manager
├── challenge/
│   ├── WakeChallenge.kt              # Challenge base interface & state
│   └── impl/                         # HoldToWakeChallenge & coming-soon stubs (NFC, QR, Steps)
├── scheduling/
│   ├── ScheduleCalculator.kt         # Next-occurrence calculation interface
│   ├── FixedTimeCalculator.kt        # Fixed-time & repeat weekday calculator
│   └── ...ScheduleCalculator.kt      # Sunrise and FirstMeeting stubs
└── ui/
    ├── alarms/                       # Alarms destination, hero card, alarm items, FAB
    ├── editor/                       # Alarm editor screen, time spinner, repeat chips
    ├── ringing/                      # RingingActivity & full-screen waking experience
    ├── sources/                      # Sound sources screen, local previews, mock Spotify
    ├── settings/                     # Settings screen, theme selector, permissions
    ├── common/                       # Tether motif canvas divider, permission rationale card
    ├── navigation/                   # DraglineNavHost and destinations
    └── theme/                        # Dark-first aesthetic, typography, shapes
```

---

## Setup & Build Instructions

### Prerequisites
- **Java**: JDK 17 (e.g. OpenJDK 17)
- **Android SDK**: API level 36 installed

### Building the Project
Clone the repository and build the debug APK:
```bash
# Make gradlew executable
chmod +x gradlew

# Run unit test suite
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```
The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Spotify Integration Status

**Spotify is mandatory for the complete personal version, but its integration is not implemented or approved.** The current `FakeSpotifyProvider` is preview data, not a working Spotify connection.

The [Spotify Developer Policy](https://developer.spotify.com/policy), section III.1, requires written approval for alarm functionality. Section IV.1 requires Premium for platform music streaming. Developer credentials alone do not establish approval for Dragline's alarm use case.

[Stage 0 of the roadmap](docs/ROADMAP.md#4-stage-0---resolve-external-dependencies) covers current policy/access research, owner-managed approval, and an approved on-device playback prototype. It must establish the supported background and locked-screen behavior before production integration. Approval is not assumed.

This repository does not yet define a working OAuth setup. It also does not assume that Dragline can cache Spotify audio or that a particular SDK will provide the required behavior. **Offline fallback means bundled local audio, not downloaded Spotify tracks.** Adding automatic recovery for network, authorization, or playback failures is implementation work tracked in the roadmap, not an existing guarantee.

---

## Roadmap

The [approved roadmap](docs/ROADMAP.md) contains **9 stages and 45 tasks**, with owners, dependencies, completion criteria, and coding-agent handoff guidance.

| Stage | Outcome |
| --- | --- |
| 0 | Resolve Spotify approval/playback feasibility and free-at-personal-scale regional transit planning. |
| 1 | Harden the alarm lifecycle, scheduling, local audio, readiness, and persistence. |
| 2 | Connect Spotify and built-in sound variety to actual playback and history. |
| 3 | Deliver configurable wake-up tasks, combinations, strictness, and emergency control. |
| 4 | Add routine follow-through and timed wakefulness checks. |
| 5 | Implement real sunrise scheduling and its fallback policy. |
| 6 | Plan wake times from calendar events, preparation, and public transit, with per-alarm automation. |
| 7 | Evaluate the complete personal version through integrated and real-morning trials. |
| 8 | Add combined scheduling rules and expand toward external users and public distribution. |

External feasibility and alarm reliability work proceed **in parallel**. Other work can advance behind agreed subsystem boundaries, but a prototype without Spotify or the essential scheduling modes does not satisfy the complete product goal. Contributors should select a bounded roadmap task and its prerequisites rather than start a broad rewrite.

---

## License

This project is licensed under the [MIT License](LICENSE) - see the [LICENSE](LICENSE) file for details.
