# WeatherConditions-CoreLib

The **platform-agnostic domain core** for WeatherConditions — a Kotlin Multiplatform library that turns weather forecasts into activity "playability" scores. Pure Kotlin, hexagonal architecture, zero framework dependencies. Powers the WeatherConditions apps (Android/iOS/desktop/Wear) **and** the WeatherConditions MCP server.

[![KMP](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF)]() [![Targets](https://img.shields.io/badge/targets-Android%20%C2%B7%20iOS%20%C2%B7%20macOS-blue)]() ![version](https://img.shields.io/badge/version-1.1.0-green)

## What it does

Given a location's forecast and an activity **profile** (e.g. golf, dog-walking), it computes a 0–100 **playability score** with an explainable breakdown of contributing factors. It also owns profile storage, sync, and the scoring engine — all as testable, dependency-free domain logic.

## Architecture (Hexagonal / CLEAN)

The core defines *what* the system does and depends on nothing platform-specific; adapters plug into the **outbound ports**:

```
core/
├── domain/
│   ├── model/        Venue, WeatherPeriod, PlayabilityProfile, ActivityScore,
│   │                 ScoringContext, PlayabilityScoreBreakdown, DefaultProfiles…
│   ├── usecase/      PlayabilityCalculator, WeatherConditionScorer, ActivityScorer,
│   │                 DogWalkingScorer, ScorerRegistry, WeatherAlertManager, ProfileSync
│   └── sync/         ProfileSyncManager, ProfileSyncProtocol (PSK/HKDF), OkioProfileBookStore
├── ports/outbound/   ForecastService, LocationService, VenueRepository, SettingsRepository,
│                     ReverseGeocodingService, NotificationService, BackupService, Logger, Clock
└── presenter/        VenuePresenter, VenueUiState (shared presentation logic)
```

## Key capabilities

- **Scoring engine** — `PlayabilityCalculator` / `WeatherConditionScorer` produce scores plus a `PlayabilityScoreBreakdown` of `ScoreFactor`s, so results are explainable.
- **Pluggable activity scorers** — `ActivityScorer` + `ScorerRegistry` (e.g. `DogWalkingScorer`) let new activities be added without touching the core.
- **Profiles** — `PlayabilityProfile` / `ProfileBook` with default profiles and CRUD via `ProfileSync`.
- **Cross-device profile sync** — `ProfileSyncManager` + `ProfileSyncProtocol` with pairing-code-derived PSK (`sha256`/HKDF) and last-write-wins merge.
- **Ports, not frameworks** — every side effect (network, location, storage, notifications, clock) is an interface, making the core trivially unit-testable.

**Targets:** `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `iosX64`, `macosArm64`, `macosX64`.

## Install

Published to **GitHub Packages** (internal):

```kotlin
repositories {
    maven("https://maven.pkg.github.com/tjmtic/WeatherConditions-CoreLib") {
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
implementation("com.abyxcz.weatherconditions.core:core-lib:1.1.0")
```

## Usage

```kotlin
val score = PlayabilityCalculator().calculateScore(context, profile)
println("${score.value}/100 — ${score.factors.joinToString { it.label }}")
```

Provide `actual` adapters for the outbound ports (a `ForecastService` over your weather API, a `VenueRepository`, etc.). The **WeatherConditions MCP server** consumes this same core to expose `score_location` / `rank_locations` tools to LLM agents.

## License

See repository. © Tim McArdle / abyxcz.
