# Dragline

**Dragline** is an open-source smart alarm for Android designed for people who become accustomed to ordinary alarm sounds and dismiss them half-asleep.

Instead of repeatedly playing one familiar alarm tone, Dragline is built to select a different song each morning from a user-selected Spotify playlist or music provider, carefully avoiding recently played tracks so the alarm remains unfamiliar without ruining your favorite songs through repetition.

Dragline also eliminates conventional one-tap snoozing. When the alarm fires, the user must complete a configurable wake-up task—such as the bundled **Hold to Wake** challenge, or future physical challenges like scanning an NFC tag across the room, scanning a QR code in the kitchen, or walking a designated step count.

**Reliability is the highest priority.** Network dropouts, Spotify auth expirations, or account contention must never prevent an alarm from sounding. Every alarm enforces a guaranteed offline local fallback tone.

---

## Current Capabilities (v0.1.0 Skeleton)

- **Compose App Shell**: Polished Material 3 interface with three primary destinations:
  - **Alarms** (Default destination): Next scheduled alarm countdown hero, active alarm list, toggle switches, repeat badges, delete controls, and empty states.
  - **Sound Sources**: Offline bundled audio tones with live previews, fallback status, and mock Spotify playlists in developer preview mode.
  - **Settings**: Theme selection (Dark, Light, System Default), default fallback sounds, recent-song exclusion window (7, 14, 30 days), gradual volume duration, vibration preferences, system permission diagnostics, and open-source information.
- **Alarm Editor**:
  - Fixed-time scheduling with hour/minute spinners and AM/PM toggle.
  - Repeat day chips (Monday through Sunday).
  - Contextual scheduling concepts (Fixed Time active; Sunrise and Calendar Meeting modeled as extensible domain types and labeled "Coming soon").
  - Sound source and wake challenge pickers.
  - Gradual volume increase (whisper-to-alarm ramp) and vibration toggles.
  - Local fallback sound selection.
- **Reliable Alarm Execution**:
  - Scheduled via Android `AlarmManager.setAlarmClock` with fallback to `setExactAndAllowWhileIdle`.
  - Permission rationale cards for `SCHEDULE_EXACT_ALARM` and `POST_NOTIFICATIONS` without nagging.
  - Automatic alarm restoration after device reboot or app update via `BootReceiver`.
  - Foreground service (`AlarmRingingService`) playing bundled audio (`res/raw/beacon.wav`, `res/raw/resonance.wav`) with smooth volume interpolation.
- **Full-Screen Ringing Experience**:
  - Displays over lock screen with `showWhenLocked="true"` and `turnScreenOn="true"`.
  - Live ticking digital clock, alarm label, active audio source, and local fallback indicator.
  - **Hold to Wake** challenge requiring a continuous 3-second press on the tether button with an animated circular progress ring and haptic feedback.
  - **No snooze button.** Alarms only dismiss upon completing the challenge.
  - Clearly documented **Debug Dismiss** escape hatch available exclusively in `debug` builds.
- **Audio Provider Abstraction & Smart Track Selection**:
  - Provider-neutral architecture (`SoundProvider`) with `LocalSoundProvider` (offline) and `FakeSpotifyProvider` (mock preview).
  - `SelectTrackUseCase`: Deterministic track selection algorithm that filters out tracks played within the configured exclusion window and gracefully falls back to the least-recently played track when all have been heard.
- **Persistence**:
  - Room database (`DraglineDatabase`) for alarms and track play history with Flow-based reactivity.
  - Local settings persistence for user preferences.

---

## Technical Stack

- **Language**: Kotlin 2.3.20
- **UI Framework**: Jetpack Compose with Material 3 & Navigation Compose
- **Architecture**: Pragmatic Layered Architecture (UI, Domain, Data, Alarm, Audio, Challenge, Scheduling)
- **Dependency Injection**: Dagger Hilt 2.60.1
- **Persistence**: Room 2.8.4 (with KSP 2.3.11) & SharedPreferences Flow
- **Build System**: Android Gradle Plugin 9.0.1, Gradle 9.1.0 (Kotlin DSL)
- **Target SDK**: 36 | **Minimum SDK**: 26 (Android 8.0+) | **Java**: 17

For an in-depth dive into subsystem interactions, sequence diagrams, and reliability guarantees, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

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
│   └── service/AlarmRingingService.kt# Foreground audio, vibration, and wake lock service
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
- **Android SDK**: API level 36 with Build Tools 35.0.0+ installed

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

## Spotify OAuth Setup (Future Integration)

In this skeleton, Spotify functionality is provided by `FakeSpotifyProvider` in **Preview Mode**. When ready to implement live Spotify App Remote or Web API streaming:

1. Create a Spotify Developer application in the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Register your package name (`com.mcasillas.dragline`) and SHA-1 fingerprint.
3. Configure Redirect URI (e.g., `dragline://callback`).
4. In `local.properties` (never committed to git), declare:
   ```properties
   SPOTIFY_CLIENT_ID=your_spotify_client_id_here
   SPOTIFY_REDIRECT_URI=dragline://callback
   ```
5. Implement `SpotifySoundProvider` implementing `SoundProvider`. If token refresh fails, network drops, or playback is interrupted, the alarm will automatically fall back to `LocalSoundProvider`.

---

## Scope Boundaries & Limitations

In accordance with this skeleton milestone:
- **No real Spotify credentials or OAuth**: Simulated via `FakeSpotifyProvider`.
- **No location or calendar permissions**: Sunrise and First Meeting schedules are represented via domain interfaces with stub calculations.
- **Physical wake challenges**: NFC, QR code, and Step Count challenges are defined as extensible models and displayed as "Coming soon" previews without requesting unnecessary permissions.

---

## Roadmap

1. **Spotify Integration**: Implement Spotify App Remote SDK and Web API authentication with automated offline caching and token refresh.
2. **Physical Wake Challenges**:
   - NFC tag scanning via Android `NfcAdapter`.
   - QR code scanning via ML Kit Barcode Scanning.
   - Step counting challenge via Android `Sensor.TYPE_STEP_COUNTER`.
3. **Contextual Scheduling Engine**:
   - Astronomical sunrise/sunset calculator based on coarse location.
   - Android Calendar Provider integration for meeting-relative wake times.
4. **Enhanced Visuals**: Extended thread / dragline interactive canvas animations for the ringing screen.

---

## License

This project is licensed under the [MIT License](LICENSE) - see the [LICENSE](LICENSE) file for details.
