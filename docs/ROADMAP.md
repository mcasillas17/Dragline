# Dragline Roadmap

Status: approved product direction, stage structure, and task baseline.
Prepared: 2026-09-10.
Implementation baseline: `c2a662ca0cf63fd8ad480597527f65edb4eb612d`.

This document combines the product brief, implementation audit, staged roadmap,
and handoff rules for coding agents. Unchecked tasks are future work, not claims
about capabilities already delivered.

## 1. Product direction

Dragline should help someone who ignores, snoozes, or dismisses more than five
alarms become consciously awake, start their morning routine, and stay awake.
The problem includes unconscious dismissal, knowingly staying in bed, and
falling asleep again after intending to get up.

The intended experience is a coordinated wake-up session:

1. Choose an appropriate wake-up time.
2. Sound an alarm that does not become excessively familiar.
3. Require deliberate engagement, including tasks that can require getting up.
4. Support the transition into a chosen morning routine.
5. Check that the user has not simply gone back to sleep.

For the initial user, routine activities include putting on shoes, walking the
dog, and making coffee. These are examples for configurable routines, not
hard-coded activities or claims the phone can automatically verify.

### Confirmed requirements

| Area | Decision |
| --- | --- |
| Initial audience | Solve the owner's personal wake-up problem first; expand later. |
| Initial device | Pixel 9 Pro XL on the user's latest installed Android. Record the actual version/build before device trials. |
| Sounds | Spotify playlists and varied built-in sounds are both required. |
| Spotify | A hard product requirement. The user has Premium, but no developer setup or alarm-specific approval. |
| Tasks | Choose or combine different wake-up tasks depending on the morning. Introduce QR, NFC, and steps incrementally. |
| Follow-through | Combine routine checkpoints with timed wakefulness checks and re-alerting when a check is missed. |
| Strictness | Configurable per alarm, rather than a universal prohibition on snoozing. |
| Emergency control | Even the strictest alarm must retain a deliberate emergency stop, separate from ordinary snooze. |
| Scheduling | Fixed-time, sunrise, and calendar-aware modes are essential to the complete personal version. |
| Calendar planning | Account for meeting location, travel, preparation, and appropriate buffers rather than only a fixed meeting offset. |
| Automation | Configurable per alarm; do not silently impose one automatic-rescheduling policy. |
| Calendar access | Work with calendars the user makes available on the phone, not just a single named cloud provider. |
| Travel | Public transit in the Seattle area, including Redmond and Bellevue. |
| Routing privacy | External routing requests are acceptable with clear disclosure and only necessary data. |
| Operating cost | External services must be free at personal usage levels. No paid subscription or billing setup is authorized by this roadmap. |
| Combined schedules | Separate modes first; combining sunrise and meeting rules belongs in a later stage. |
| Delivery | Coding agents implement bounded tasks; the owner directs, reviews, and handles external approvals. |

An offline-only prototype, a Spotify-only alarm without follow-through, or a
fixed-time-only release does not satisfy the complete product goal. Earlier
builds can still be valuable learning milestones.

### Initial scope boundaries

- No broad architecture rewrite or visual redesign is required to start.
- A Dragline cloud account, backend, synchronization service, and learned/LLM
  scheduling are not requirements established by this interview. Do not add
  them implicitly; justify any later proposal against privacy and cost limits.
- Do not promise that an alarm can sound under every possible device condition.
  Define supported conditions, expose readiness problems, and document limits.
- Do not represent a completed scan, step count, or self-reported checkpoint as
  proof of consciousness or proof that a real-world activity was completed.
- Public release and support for a broad device fleet come after personal use.
- Work is milestone-based. No delivery date or effort estimate has been agreed.

## 2. Current implementation audit

The audit covered the README, architecture document, initial implementation PR,
build/CI configuration, scheduling and ringing paths, audio providers, challenge
logic, screen/view-model wiring, persistence, and all four unit-test files.
The worktree was clean at the audited revision.

This was a source-level audit, not a device test or an exhaustive security audit.
The latest Android CI run inspected for the baseline revision reported success;
no local build or device run was performed as part of this audit.

### Existing foundation

The repository contains a native Kotlin/Compose application with Hilt, Room,
local preferences, alarm editing, fixed-time/repeating schedules, two bundled
tones, a ringing foreground service, Hold to Wake, and permission status cards.
These components provide a useful starting point.

| Finding | Evidence | Roadmap consequence |
| --- | --- | --- |
| Music rotation is disconnected from ringing. The selector and history repository exist, but the ringing service directly chooses local audio. | [Ringing service](../app/src/main/java/com/mcasillas/dragline/alarm/service/AlarmRingingService.kt), [selector](../app/src/main/java/com/mcasillas/dragline/domain/usecase/SelectTrackUseCase.kt), [history repository](../app/src/main/java/com/mcasillas/dragline/data/repository/TrackHistoryRepositoryImpl.kt). Exact identifier and playback/history call-site searches found definitions and tests, not a ringing integration. | Connect selection, actual playback, fallback, and history end to end in Stage 2. |
| The selected local primary sound is ignored when ringing: both source branches use `localFallbackSound`. | The [editor](../app/src/main/java/com/mcasillas/dragline/ui/editor/AlarmEditorViewModel.kt) persists separate selections; the ringing service uses the fallback in both branches. | Correct primary/fallback separation in Stage 1. |
| Playback failure does not initiate further recovery. | [AudioPlayer](../app/src/main/java/com/mcasillas/dragline/audio/AudioPlayer.kt) returns `false`; the ringing service ignores the result and still publishes an active state. | Establish explicit playback outcomes, recovery, and honest UI state. |
| Interrupted and overlapping alarms need an explicit lifecycle model. | The ringing service stores one `currentAlarm` in memory, returns immediately for a null restart intent, and schedules the next repeat during dismissal. These are source-confirmed risks requiring lifecycle reproduction. | Separate configuration, scheduled occurrences, and active wake sessions; define overlap and recovery rules. |
| A sound preview can interfere with alarm audio. | The [source view model](../app/src/main/java/com/mcasillas/dragline/ui/sources/SoundSourcesViewModel.kt) and service share the singleton AudioPlayer; preview play, stop, and cleanup manipulate it. | Give active ringing exclusive playback ownership and isolate previews. |
| Physical and contextual features are placeholders. | [Challenge types](../app/src/main/java/com/mcasillas/dragline/domain/model/WakeChallengeType.kt) mark NFC/QR/steps unimplemented. [Sunrise](../app/src/main/java/com/mcasillas/dragline/scheduling/SunriseScheduleCalculator.kt) and [meeting](../app/src/main/java/com/mcasillas/dragline/scheduling/MeetingScheduleCalculator.kt) calculators use fixed 06:00 and 09:00 placeholders. | Implement real inputs and failure rules, rather than merely enabling their pickers. |
| Device-readiness and scheduling-change coverage is narrow. | [Manifest](../app/src/main/AndroidManifest.xml) and [BootReceiver](../app/src/main/java/com/mcasillas/dragline/alarm/BootReceiver.kt) register boot/package restoration. Inspected screen paths check permissions at initialization/composition; lifecycle-symbol searches did not identify time-change or full-screen-readiness handling. | Define a supported-device recovery and readiness matrix. |
| Database upgrade behavior is unsafe for a relied-upon alarm app. | [DatabaseModule](../app/src/main/java/com/mcasillas/dragline/di/DatabaseModule.kt) enables destructive migration fallback. | Establish migration and backup/restore behavior before personal reliance. |
| Current tests cover small units, not the wake-up lifecycle. | The four files under [app/src/test](../app/src/test/java/com/mcasillas/dragline) contain 16 tests: six fixed-schedule, four track-selection, four fake-DAO repository/mapper, and two hold-duration cases. Exact/fragment test-file searches and the `app/src` directory inventory identified only `main` and `test`. | Add focused integration/device evidence for scheduling, ringing, recovery, and challenge behavior. |
| Documentation overstates or differs from execution. | README describes an exact-and-idle fallback while [scheduler code](../app/src/main/java/com/mcasillas/dragline/alarm/AndroidAlarmScheduler.kt) uses `setAndAllowWhileIdle`. Architecture text describes provider-driven Spotify fallback and a receiver wake lock, while the inspected paths bypass providers and simply start the service. Version text also lags build configuration. | Keep implementation facts, future goals, and bounded reliability claims distinct. |

The main architectural need is to connect existing boundaries and establish
lifecycle ownership. Small wrappers or unused extension points are lower
priority than correctness; do not turn this roadmap into a general cleanup.

### External dependency: Spotify

The [Spotify Developer Policy](https://developer.spotify.com/policy), retrieved
for this roadmap and labeled effective 15 May 2025, states in section III.1 that
alarm functionality requires Spotify's written approval. Section IV.1 restricts
platform music streaming to Premium subscribers.

Premium is available to the initial user. Approval is not. The approved plan is
to pursue feasibility while hardening the alarm foundation in parallel.

This roadmap does not assume approval will be granted, that a self-service
approval channel exists, that Spotify audio can be cached by Dragline, or that a
particular SDK will support the required background experience. Those questions
must be resolved through current primary sources and an approved prototype.
Do not substitute scraping, extracted audio, or an unapproved playback route.

## 3. Delivery structure

| Stage | Outcome | Dependencies |
| --- | --- | --- |
| 0 | Spotify and transit feasibility, with explicit decision gates | Starts immediately |
| 1 | Reliable alarm foundation | Starts alongside Stage 0 |
| 2 | Spotify and built-in sound variety | Stage 1; Spotify implementation also requires approval |
| 3 | Configurable wake-up tasks | Stage 1 lifecycle and readiness foundations |
| 4 | Routine follow-through and wakefulness checks | Stage 3 wake-session contract |
| 5 | Sunrise scheduling | Stage 1 scheduling contract |
| 6 | Calendar and transit-aware planning | Stage 0 transit feasibility and Stage 1 scheduling |
| 7 | Complete personal daily-use version | All essential outcomes from Stages 0-6 |
| 8 | Combined rules and expansion | Personal acceptance; separate owner authorization for public release |

Numbering is organizational, not a mandate to serialize independent work.
For example, challenge and sunrise work can progress while Spotify approval is
pending. The complete personal-product gate must not be renamed or weakened to
hide an unresolved hard requirement.

### Task conventions

- `A` means coding/research agent; `O` means owner; `A+O` means agent prepares
  evidence or implementation and owner makes the named decision.
- IDs are stable references for future issues, branches, and task dependencies.
- Check a task only after its deliverable and acceptance evidence are reviewed.
- A decision task is concrete work: its output is a recorded choice and
  acceptance examples. It is not permission for an agent to invent a policy.
- Before implementing a subsystem, produce a focused design for that subsystem,
  referencing this document. Do not attempt one giant implementation plan.
- Each implementation task should include relevant tests, user-visible error
  behavior, and directly related documentation. Existing project tools are the
  default; do not add infrastructure merely to complete a checklist.

## 4. Stage 0 - Resolve external dependencies

**Goal:** establish whether the mandatory integrations can be delivered within
policy, device, privacy, and cost constraints. Run alongside Stage 1.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S0-01: Assess Spotify approval and documented integration options | A | Review current official policy, developer access rules, SDK/API capabilities, and available contact routes. Record sources, remaining uncertainties, and the exact proposed alarm use case. Do not promise approval or background playback from an API inventory alone. |
| [ ] S0-02: Obtain the required Spotify authorization | O, with agent-prepared materials; S0-01 | Owner handles developer setup and any approval correspondence. Record written approval covering the intended use before implementing Spotify alarm functionality. A denial or unresolved request leaves the Spotify gate blocked and requires an explicit product decision; it does not authorize an offline-only substitute. |
| [ ] S0-03: Prove the approved Spotify playback path on the target phone | A+O; S0-02 | Build a bounded approved prototype and record playlist access, playback confirmation, locked-screen/background behavior, expired authorization, missing network, playback interruption, and account/device contention. Establish which failures can recover and when local fallback takes over. Do not claim success from foreground playback alone. |
| [ ] S0-04: Establish a viable transit-data and routing approach | A+O | Compare current primary sources for Seattle/Redmond/Bellevue coverage, arrival-time routing, walking/transfer legs, schedule versus live information, location lookup, access terms, and quotas. Demonstrate representative regional journeys, including early service and no-route cases. Document requests per planned alarm and a daily cap that fits verified free personal use. Do not enable billing or transmit the owner's private itinerary during research. |
| [ ] S0-05: Approve scheduling and wake-session behavior contracts | A+O | Record examples for task combination/order, strictness modes, routine/check timing, calendar eligibility, preparation/buffers, automation modes, fallback times, and overlap handling. Approve each subsystem's contract independently, before its implementation begins; unrelated external gates must not block that discussion. Defaults must be explicit, not scattered across view models. |

**Gate:** approved, demonstrated Spotify path for Stage 2 Spotify integration;
verified usable free transit approach for Stage 6 automatic transit estimates.
Record blocked gates visibly. Calendar-mode UI and pure planning logic may be
developed independently, but manual offsets do not fulfill transit-aware planning.

## 5. Stage 1 - Reliable alarm foundation

**Goal:** make alarm execution an explicit, recoverable system before adding more
ways to trigger, silence, or extend it.

Target separation: alarm configuration -> planned occurrence -> active wake
session -> optional routine session. Keep the existing app/layer structure unless
a specific task demonstrates a need for another boundary.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S1-01: Define occurrence and wake-session state | A+O; S0-05 overlap/lifecycle decisions | Model stable occurrence identity, active state, completion, emergency exit, failure, and recovery separately from alarm configuration. Define duplicate/stale trigger and overlapping-alarm behavior with transition tests. A second alarm must not silently replace the first. |
| [ ] S1-02: Make ringing startup, recovery, and teardown coherent | A; S1-01 | Implement the approved lifecycle in the service. Address restart without an intent, asynchronous startup ordering, canceled work, wake-lock release, dismissal identity, and state observed by the ringing UI. Reproduce interruption and overlap cases; no success-shaped state after failed startup. |
| [ ] S1-03: Reconcile scheduling and persistence | A; S1-01 | Cover save, edit, enable, disable, delete, firing, recurrence, reboot, app update, and relevant clock/time-zone changes. Advance repeats according to the approved occurrence policy rather than relying solely on successful dismissal. Verify duplicate, stale, overdue, and daylight-saving cases without inventing a one-off alarm's intended date. |
| [ ] S1-04: Establish reliable local audio and preview isolation | A; S1-01 | Play the selected local primary tone, reserve the fallback for failures, and give ringing exclusive audio ownership. Handle initialization and ongoing playback errors, volume ramping, audio routing/interruption, and cleanup. Demonstrate that starting/stopping/leaving a preview cannot stop or replace a ringing alarm. Surface inability to produce sound. |
| [ ] S1-05: Implement truthful readiness and onboarding | A; S1-02, S1-03 | Assess the permissions and device settings relevant to supported behavior, including exact scheduling, notifications, and full-screen presentation. Refresh status when returning from settings. Distinguish saved, scheduled, degraded, and blocked alarms; provide a local rehearsal flow. Verify the applicable Android requirements against current official documentation. |
| [ ] S1-06: Protect persisted alarms across upgrades and restore | A; S1-01 | Replace destructive migration as the production upgrade strategy, define migration coverage and backup/restore scope, and reconcile restored schedules with the OS. Demonstrate that supported upgrades retain alarm settings without inventing active sessions or replaying obsolete ones. |
| [ ] S1-07: Establish device evidence and diagnostics | A+O; S1-02 through S1-06 | Record the Pixel's installed OS/build. Exercise ringing while locked/idle, process interruption, reboot, denied/revoked permissions, primary playback failure, overlapping alarms, and dismissal cleanup. Add concise local diagnostics with occurrence IDs and reasons, not credentials or unnecessary calendar/location detail. Record observed limitations and reproducible failures. |

**Gate:** the agreed foundation scenarios have reviewed evidence on the target
phone, including audible fallback and continued recurrence after interruption.
This is not a universal alarm guarantee. Keep the existing trusted alarm as a
backup during prototype evaluation.

## 6. Stage 2 - Music that stays varied

**Goal:** deliver the mandatory Spotify experience and useful built-in variety
through one observable selection/playback pipeline.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S2-01: Connect selection, playback outcomes, and history | A; Stage 1 | Reuse the existing selector where suitable. Define provider-scoped track identity, exclusion rules, exhaustion behavior, and what counts as an actual play. Record confirmed playback, not an unsuccessful selection; fallback must not mark an unheard Spotify song as played. Cover empty, changed, and exhausted collections. |
| [ ] S2-02: Implement authorized Spotify account and playlist access | A; S0-03 | Add the approved authentication lifecycle, playlist selection, connection state, and disconnect/data-removal behavior. Cover expired/revoked access and unavailable content; do not commit credentials or couple core ringing to token retrieval. |
| [ ] S2-03: Integrate Spotify playback and bounded fallback | A; S2-01, S2-02 | Wire the proven approved playback path into ringing with explicit startup/interruption outcomes and a bounded fallback policy. Verify the chosen track actually starts, required metadata presentation is satisfied, and local audio takes over on the supported failure cases. Stop Spotify and fallback correctly at session transitions. |
| [ ] S2-04: Expand built-in sound variety | A+O; S2-01 | Add owner-approved sounds with documented rights to distribute, distinguishable sound identities, and agreed rotation rules. Exercise preview, chosen primary, fallback, exclusion, and volume behavior. Do not derive or redistribute Spotify audio. |
| [ ] S2-05: Replace mock product surfaces with truthful source status | A; S2-03, S2-04 | Update editor, sources, settings, and ringing UI consistently. Display the actual playing source and fallback reason, and expose unavailable configurations before bedtime. Remove or clearly isolate developer-only mock data from the usable product path. |

**Gate:** an approved Spotify playlist produces selected tracks on the target
phone, history affects subsequent choices, built-in variety works, and the
documented failure scenarios produce the intended fallback.

## 7. Stage 3 - Configurable wake-up tasks

**Goal:** interrupt automatic dismissal while allowing different approaches for
different mornings. Reuse Hold to Wake as an option, not a substitute for all
physical tasks.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S3-01: Implement task-session and strictness policies | A+O; Stage 1, S0-05 task decisions | Support the approved task combination/order and per-alarm strictness modes. Persist enough state for the chosen recovery policy. Define ordinary snooze, task-time quiet periods, missed deadlines, and completion separately; a UI recreation must not bypass the required work. |
| [ ] S3-02: Add QR checkpoint setup and verification | A; S3-01 | Let the user enroll, name, try, replace, and remove a QR checkpoint. Complete only on the configured match. Cover camera denial, wrong/unreadable codes, unavailable checkpoint, interruption, and emergency exit. |
| [ ] S3-03: Add NFC checkpoint setup and verification | A; S3-01 | Let the user enroll and try a tag, then require the configured match during a task. Cover unavailable/disabled NFC, a wrong/lost tag, app lifecycle changes, and emergency exit. Define the chosen identifier/payload semantics explicitly. |
| [ ] S3-04: Add step-count tasks | A; S3-01 | Establish a task-local starting count, show progress, and enforce the selected target. Handle capability/permission denial, counter reset, stale readings, interruption, and unsupported hardware without fabricated progress or a trapped alarm. |
| [ ] S3-05: Integrate task configuration and accessible completion | A; S3-02 through S3-04 | Wire task choices and combinations through persistence, editor, ringing, and rehearsal. Provide usable status and controls for supported accessibility inputs. Confirm that unsupported tasks cannot be silently saved as usable. |
| [ ] S3-06: Deliver emergency stop in release behavior | A; S3-01 | Provide an intentional, accessible emergency path in every strictness/task state, not only debug builds. Stop active audio/vibration and pending re-alerts for that session, release resources, and record an emergency exit separately from successful wake-up. Do not disable unrelated future alarms. |

**Gate:** tasks and configured combinations work on the target phone, strictness
matches configuration, and an unavailable task never removes emergency control.

## 8. Stage 4 - Morning follow-through

**Goal:** help the user stay awake after the first alarm becomes quiet.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S4-01: Model configurable routine checkpoints | A+O; S3-01, S0-05 routine decisions | Define named steps, order, timing, and completion evidence. Support shoes/dog-walk/coffee as user-authored examples. Separate manual acknowledgement from QR/NFC/step evidence; do not infer completion from an activity's label. |
| [ ] S4-02: Implement routine progress and timed checks | A; S4-01, Stage 3 | Transition a wake session into routine support, schedule the agreed wakefulness checks, and display what is due next. Handle backgrounding, interruption, delayed actions, and completed checkpoints without losing or duplicating progress. |
| [ ] S4-03: Implement missed-check re-alerting and exit behavior | A; S4-02, S3-06 | Apply the selected per-alarm escalation/strictness policy when a check is missed. Define bounded retry/escalation behavior. Completion and emergency exit cancel that session's pending checks; no orphaned alarm should fire during a later dog walk or unrelated activity. |
| [ ] S4-04: Add a minimal personal wake-up record | A+O; S4-03 | Record the information needed to evaluate the product: scheduled/actual start, source/fallback, task/routine outcomes, missed checks, and optional self-report of returning to sleep. Define local retention and deletion. Do not require a cloud account or continuous location tracking to measure progress. |

**Gate:** an end-to-end session can ring, become quiet after its task, guide a
routine, re-alert on a missed check, and finish or exit with no pending artifacts.

## 9. Stage 5 - Sunrise scheduling

**Goal:** replace the placeholder with a real, independent sunrise alarm mode.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S5-01: Define sunrise location and fallback policy | A+O; Stage 1 | Agree on configured versus device location, location freshness, offsets, travel/time-zone handling, and the explicit backup time when a valid solar plan cannot be obtained. Request only the data needed; do not make background tracking an implicit requirement. |
| [ ] S5-02: Implement and test solar occurrence calculation | A; S5-01 | Select a suitable implementation using verified sources and licensing. Cover dates, zones, repeat days, offsets crossing dates, daylight-saving changes, and locations/dates without a valid sunrise. Replace the hard-coded 06:00 behavior rather than relabeling it. |
| [ ] S5-03: Integrate sunrise planning and explanations | A; S5-02, S1-03 | Persist and schedule the calculated occurrence, show location/date/offset and resulting time, and identify stale data or fallback use. Verify recalculation and offline behavior on the target phone. |

**Gate:** the selected sunrise location and offset produce explained,
repeatable occurrences and a defined backup when inputs are unavailable.

## 10. Stage 6 - Calendar and transit-aware planning

**Goal:** derive an understandable wake-up plan from when the user must be ready
or arrive, not simply subtract an arbitrary offset from the first event.

Conceptual calculation for an in-person event:

`wake time = required leave-home time - preparation duration - configured buffer`

The journey planner supplies a leave-home time that accounts for the selected
arrival deadline, access walking, departures, transfers, and destination walking
to the extent supported by the verified provider. Avoid double-counting buffers
or walking already included in a route.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S6-01: Read and select eligible on-device calendar events | A+O; Stage 1, S0-05 eligibility decisions | Enumerate calendars available through the chosen supported phone integration and let the user select them. Handle recurring/all-day/canceled/declined/duplicate events, time zones, and missing access. Make actual calendar availability explicit; do not promise every provider exposes all data. |
| [ ] S6-02: Model preparation and meeting context | A+O; S6-01 | Capture owner-approved preparation durations, departure origin policy, arrival buffer, relevant event windows, and online versus in-person handling. Resolve or ask about ambiguous locations instead of guessing. Define no-meeting and multiple-candidate behavior. |
| [ ] S6-03: Implement the verified transit adapter | A; S0-04, S6-02 | Query only needed journey inputs; distinguish scheduled from live information. Cover no service, missed connections, stale responses, quota exhaustion, outages, and unavailable location lookup. Apply bounded requests/retries and the validated free-use cap; avoid continuous polling. |
| [ ] S6-04: Build the explainable wake-plan calculator | A; S6-02, S6-03 | Produce an occurrence with a breakdown of meeting, arrival target, journey, preparation, buffers, and freshness. Test representative Seattle/Redmond/Bellevue journeys and events with no feasible route, including early mornings and overnight date boundaries. Keep business-rule calculation independently testable. |
| [ ] S6-05: Implement per-alarm planning autonomy | A+O; S6-04, S0-05 automation decisions | Implement the approved modes, bounds, and overnight freeze/change policy. Cover calendar edits, route changes, a plan moving into the past, and user overrides. Show the currently armed time and why it changed; do not silently assume permission to move it earlier or later. |
| [ ] S6-06: Reconcile plans, cached inputs, and backup alarms | A; S6-05, S1-03 | Define when a previously accepted plan remains valid and when the user's explicit backup time is used. Handle offline starts, stale data, revoked permissions, and provider failures without dropping the alarm. Verify that UI, persisted plan, and system schedule agree. |
| [ ] S6-07: Validate planning on the target phone | A+O; S6-06 | Exercise online/in-person/no-event days, calendar edits, regional transit examples, overnight changes, offline operation, quota limits, and different automation settings. Review location/calendar disclosure and retention against the minimum-data requirement. |

**Gate:** real calendar inputs and a verified transit source produce an
explained, appropriately armed wake time within the approved automation policy.
Fixed manual commute offsets may be a fallback, not evidence that this stage
has delivered automatic transit planning.

## 11. Stage 7 - Complete personal daily-use version

**Goal:** assess whether the full product actually addresses the owner's morning
problem, not merely whether the screens and individual features exist.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S7-01: Agree on the personal trial protocol | A+O; can begin during earlier prototypes | Establish a baseline, trial length, and success thresholds before the trial. Measure consciously waking, starting the selected routine, returning to sleep, extra backup alarms used, false/missed checks, and disruption. Keep a trusted backup alarm during evaluation; do not equate checklist completion with wakefulness. |
| [ ] S7-02: Integrate all essential modes and configurations | A; Stages 2-6 | Demonstrate fixed, sunrise, and calendar modes with required sound choices, task combinations, routines, strictness, automation, and emergency exit. Cover representative cross-feature combinations and upgrades of saved configurations. |
| [ ] S7-03: Run real-morning trials and close attributable defects | A+O; S7-01, S7-02 | Owner uses the agreed trial setup; agents investigate reproducible defects and review privacy-conscious diagnostics. Compare outcomes with the baseline and agreed thresholds, including mornings where the user returns to sleep despite completing tasks. Do not silently lower the acceptance target. |
| [ ] S7-04: Decide personal-release readiness | O, with agent evidence; S7-03 | Review all essential feature gates, Spotify authorization, free-service usage, device limitations, accessibility/escape behavior, and remaining defects. Mark ready only by explicit owner acceptance; document any limitation rather than making a universal reliability claim. |

**Gate:** the owner accepts the complete personal version based on both
technical evidence and actual morning outcomes. A passing build alone is not
this milestone.

## 12. Stage 8 - Combined rules and expansion

**Goal:** extend a personally useful product without making these extensions
prerequisites for the first complete personal version.

| Task | Owner / dependencies | Deliverable and completion criteria |
| --- | --- | --- |
| [ ] S8-01: Design combined scheduling rules | A+O; Stage 7 | Define precedence and conflicts for examples such as sunrise unless a meeting needs an earlier wake-up. Preserve explanations, bounds, and fallback semantics; obtain approval before introducing a general-purpose rule builder. |
| [ ] S8-02: Implement approved rule combinations | A; S8-01 | Add only the approved combinations, with conflict, stale-input, cross-date, and override coverage. Existing single-mode alarms retain their behavior. |
| [ ] S8-03: Prepare a small external-user pilot | A+O; Stage 7 | Expand device/OS coverage and onboarding, review accessibility/localization needs, collect bounded feedback, and reassess Spotify access and routing quotas for the actual audience. Personal-scale free usage must not be assumed to scale indefinitely. |
| [ ] S8-04: Decide and prepare public distribution | O authorizes; A implements; S8-03 | Choose distribution and support approach, then address signing, releases, privacy disclosures, provider terms, migrations, and maintenance. Publishing, spending, or changing external service plans requires separate owner authorization. |

**Gate:** each expansion is a new explicit decision. No automatic public release
or paid infrastructure is authorized by completion of earlier stages.

## 13. Coding-agent handoff and review

For each selected task, the owner or coordinating agent should provide:

1. The task ID, approved behavior contract, and completed dependencies.
2. A bounded scope naming the existing files/subsystem to inspect first.
3. Expected observable behavior and failure cases, including what must remain
   unchanged.
4. A focused validation approach using existing project tools, plus device
   scenarios when Android behavior matters.
5. Any owner-only action or external gate that the agent cannot resolve itself.

Agents should inspect current code before choosing an implementation; file
references in this audit describe the baseline, not permanent architecture.
Do not have independent agents modify the same lifecycle/audio/persistence
surface concurrently. Parallelize only behind agreed interfaces and clear file
ownership.

For behavior changes, first capture the relevant failure or desired behavior,
then implement and produce fresh evidence. Report source reasoning, automated
results, device observations, and untested assumptions separately. Never mark
Spotify, transit, permission, or recovery behavior complete using a mock alone.

Each handoff should record changed files, meaningful behavior changes, evidence,
remaining limitations, and the status of its task gate. Keep secrets and private
calendar/location data out of commits and shared logs.

### Keeping this document current

- Preserve confirmed requirements unless the owner explicitly changes them.
- Attach decision outcomes to their task IDs so a later agent does not reopen
  settled questions or treat assumptions as approvals.
- Update task status with evidence, not optimistic estimates.
- When a gate is blocked, name the dependency, owner, and condition for resuming.
- Reconcile the README and architecture document as their corresponding
  implementation tasks land; this roadmap does not certify their current claims.
- Keep detailed implementation plans scoped to one subsystem or bounded task
  group. This document is the product-level coordination and acceptance map.
