# AGENTS

## Purpose
AI-optimized repo map for agents working in this workspace. Read this first; only crawl deeper when the task clearly needs it.

## Project Snapshot
- Product scope is mixed-purpose:
  - food analysis / calorie tracking
  - OLX seller flow with AI ad generation
  - agile estimation / team planning tools
- Tech stack:
  - Kotlin `2.3.20`
  - Compose Multiplatform `1.11.0-beta01`
  - Android Gradle Plugin `9.1.0`
  - Ktor `3.4.2`
  - Koin `4.2.0`
  - Supabase Kotlin `3.4.1`
  - Navigation3 runtime/UI
- JVM target is `11` across Android/JVM/server.

## Modules

### `composeApp`
- Shared Compose Multiplatform UI module.
- Targets:
  - Android
  - iOS framework
  - Desktop JVM
  - Web JS
  - Web Wasm
- Main responsibilities:
  - app shell and top-level navigation
  - feature UIs and view models
  - shared design system
  - Koin wiring for UI/domain layer
  - Ktor/OpenAI client setup
  - media upload, camera, file picker, permissions

### `shared`
- Cross-platform domain/config module used by app and server.
- Main responsibilities:
  - BuildKonfig-backed secrets
  - Supabase client wrapper
  - shared models / config / platform helpers

### `androidApp`
- Thin Android application wrapper around `:composeApp`.
- Hosts `MainActivity`.
- Initializes Android-specific storage and receives OLX auth deep links.

### `iosApp`
- Xcode entrypoint.
- SwiftUI wrapper around the shared Compose app/framework.

### `server`
- Minimal Ktor JVM backend.
- Depends on `:shared`.
- Current implementation is tiny; do not assume backend business logic lives here.

## Gradle Structure
- Root includes exactly:
  - `:composeApp`
  - `:androidApp`
  - `:server`
  - `:shared`
- Version catalog: `gradle/libs.versions.toml`
- `kotlin.mpp.applyDefaultHierarchyTemplate=false` is intentionally set in `gradle.properties`.

## Source Set Map

### `composeApp/src/commonMain`
- Most app logic lives here.
- High-value packages:
  - `features/` feature code
  - `designsystem/` reusable UI primitives/tokens
  - `navigation/` destination types and layouts
  - `startup/` startup state and top-level nav view model
  - `di/` Koin modules
  - `network/` Ktor/OpenAI client setup
  - `camera/`, `datastore/`, `features/media/` platform-facing abstractions

### `composeApp` platform source sets
- `androidMain`: Android actuals for camera, image conversion, datastore, OLX web view.
- `jvmMain`: Desktop entrypoint and desktop actuals.
- `jsMain` / `wasmJsMain`: web entrypoints and web actuals.
- `iosMain`: shared iOS source set exists and depends on `dataStoreMain`.
- `dataStoreMain`: common source set used by Android/JVM/iOS for datastore support.
- `jsWasmMain`: shared source set for JS + Wasm web code.

### `shared/src/commonMain`
- Core packages:
  - `supabase/`
  - `config/`
  - `platform/`
- This module is intentionally small but important.

## App Entry Points
- Android: `androidApp/src/main/kotlin/com/sirelon/aicalories/MainActivity.kt`
- Desktop: `composeApp/src/jvmMain/kotlin/com/sirelon/aicalories/main.kt`
- Web Wasm: `composeApp/src/wasmJsMain/kotlin/com/sirelon/aicalories/main.kt`
- Root composable: `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/App.kt`
- iOS: `iosApp/iosApp/iOSApp.swift`
- Server: `server/src/main/kotlin/com/sirelon/aicalories/Application.kt`

## Navigation Rules
- `App.kt` is intentionally thin. Do not move app navigation state into composables.
- Top-level destinations are defined in `navigation/AppDestination.kt`.
- Top-level back stack ownership lives in `startup/AppNavigationViewModel`.
- Startup routing logic also lives in `AppNavigationViewModel`:
  - splash
  - onboarding gate
  - seller auth/session gate
- Current top-level destinations:
  - `Splash`
  - `SellerOnboarding`
  - `SellerLanding`
  - `Seller`
  - `Analyze`
  - `History`
  - `Agile`
  - `DataGenerator`
- If adding new app-level navigation, prefer:
  1. add destination to `AppDestination`
  2. update `AppNavigationViewModel`
  3. register the entry in `App.kt`

## DI Rules
- DI framework is Koin.
- Top-level modules are registered in `composeApp/.../di/KoinModules.kt`.
- `appModule` includes feature modules; `networkModule` provides shared networking clients.
- Feature modules typically live in each feature’s `di/` package.
- Prefer adding dependencies via Koin modules, not manual singleton objects.

## Feature Layout Patterns

### Common pattern
Most features use some combination of:
- `data/`
- `di/`
- `presentation/`
- `ui/`
- `model/`

### ViewModel pattern
- Common base lives at `features/common/presentation/BaseViewModel.kt`.
- Contracts are usually split into:
  - `...Contract.kt`
  - `...ViewModel.kt`
  - screen/render layer in `ui/` or feature root file

### Feature inventory
- `features/analyze`
  - AI-powered meal/food analysis flow.
  - Uses Supabase + OpenAI-backed logic.
- `features/history`
  - Meal/history display.
  - Navigates back to `Analyze`.
- `features/seller`
  - OLX auth/onboarding/ad-generation flow.
  - Main subareas: `auth/`, `ad/`, `onboarding/`.
- `features/agile`
  - Agile estimation/team capacity tooling.
  - Contains its own sub-navigation-like flow under `AgileRoot`.
- `features/datagenerator`
  - Test/demo data generation UI.
- `features/media`
  - Upload, permission, picker, format conversion helpers shared by other features.

## Seller / OLX Flow
- This is a real project concern, not sample code.
- Important classes:
  - auth repo: `features/seller/auth/data/OlxAuthRepository.kt`
  - API client: `features/seller/auth/data/OlxApiClient.kt`
  - session/token stores: `OlxAuthSessionStore`, `OlxTokenStore`
  - redirect bridge: `OlxAuthCallbackBridge.kt`
- Deep link callback handling exists on:
  - Android via `MainActivity`
  - Web via `wasmJsMain/main.kt`
- Redirect URI default: `selolxai://olx-auth/callback`

## Analyze / Supabase Flow
- Shared Supabase wrapper: `shared/.../supabase/SupabaseClient.kt`
- Current responsibilities include:
  - auth with default test credentials when needed
  - file upload to Supabase Storage bucket `test`
  - create `food_entry`
  - link uploaded files
  - invoke edge/function `analize-food`
  - observe analysis summary/results via realtime/Postgrest
- Shared models and responses live under:
  - `shared/.../supabase/model/`
  - `shared/.../supabase/response/`
- Expect food-analysis changes to touch both `composeApp` and `shared`.

## Design System Rules
- Prefer `AppTheme.typography` and `AppTheme.colors`.
- Prefer `AppDimens` tokens over raw `dp`.
- Add new size tokens only when the exact value matters.
- Design system code lives under `composeApp/src/commonMain/kotlin/com/sirelon/aicalories/designsystem/`.
- Reusable templates already exist in `designsystem/templates/`.
- Avoid reaching for raw Material APIs first when an app component/token already exists.
- Use the 40 custom icons (`ic_*.xml`) when suitable instead of Material Design icons.

## Platform Abstractions
- Camera launcher uses expect/actual style placement under `camera/`.
- Image conversion is platform-specific under `features/media/ImageFormatConverter.*`.
- Datastore abstraction lives under `datastore/KeyValueStore*`.
- Platform checks are centralized in `shared/.../platform/PlatformTargets.kt`.

## Secrets And Config
- Secrets are resolved in `shared/build.gradle.kts` from:
  - Gradle properties
  - environment variables
  - `local.properties`
- BuildKonfig object:
  - package: `com.sirelon.aicalories.supabase`
  - object: `SupabaseConfig`
- Keys currently resolved:
  - `OPENAI_KEY` / `openai.key`
  - `SUPABASE_URL` / `supabase.url`
  - `SUPABASE_KEY` / `supabase.key`
  - `SUPABASE_DEFAULT_EMAIL` / `supabase.default.email`
  - `SUPABASE_DEFAULT_PASSWORD` / `supabase.default.password`
  - `OLX_CLIENT_ID` / `olx.client.id`
  - `OLX_CLIENT_SECRET` / `olx.client.secret`
  - `OLX_SCOPE` / `olx.scope`
  - `OLX_AUTH_BASE_URL` / `olx.auth.base.url`
  - `OLX_API_BASE_URL` / `olx.api.base.url`
  - `OLX_REDIRECT_URI` / `olx.redirect.uri`
- Fallback defaults exist for local/dev builds; do not mistake them for production values.

## Important Build Notes
- `composeApp/build.gradle.kts` has a custom Compose resources setup for Android.
- Do not remove the `copyComposeResourcesToAndroidAssets` workaround if you encounter it elsewhere in the file/history.
- `copyAndroidMainComposeResourcesToAndroidAssets` may be disabled intentionally to avoid build failures.
- `compose.resources` generates public resources class:
  - package `com.sirelon.aicalories.generated.resources`
- Android resources are enabled for the KMP library target.

## Common Commands
- Build Android debug: `./gradlew :composeApp:assembleDebug`
- Run desktop app: `./gradlew :composeApp:run`
- Run server: `./gradlew :server:run`
- Run web Wasm: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- Run web JS: `./gradlew :composeApp:jsBrowserDevelopmentRun`
- Run all tests: `./gradlew allTests`
- Android lint: `./gradlew lint`
- JS tests: `./gradlew jsTest`
- JVM tests: `./gradlew jvmTest`
- iOS simulator tests: `./gradlew iosSimulatorArm64Test`

## Fast “Where Do I Edit?” Guide
- Add app-level screen/navigation:
  - `navigation/AppDestination.kt`
  - `startup/AppNavigationViewModel.kt`
  - `App.kt`
- Add a feature dependency or ViewModel:
  - feature `di/*Module.kt`
  - `di/KoinModules.kt` if it is a new top-level feature module
- Change app theme/tokens/components:
  - `designsystem/`
- Change Supabase or secret-backed config:
  - `shared/build.gradle.kts`
  - `shared/src/commonMain/kotlin/com/sirelon/aicalories/config/`
  - `shared/src/commonMain/kotlin/com/sirelon/aicalories/supabase/`
- Change OLX auth behavior:
  - `features/seller/auth/`
- Change Android deep link behavior:
  - `androidApp/.../MainActivity.kt`
- Change web callback behavior:
  - `composeApp/src/wasmJsMain/.../main.kt`

## Documentation / Lookup Rules
- For Android or Google APIs/libraries, use the Google dev MCP tools instead of memory or generic web search.
- Good examples:
  - Jetpack Compose
  - AndroidX
  - Material
  - Google Play services
  - Firebase

## API Response Class Conventions

These rules apply to every class that directly maps a JSON API response (OLX, Supabase, or any external service). Violating them will be rejected in review.

### Naming
- Suffix is `Response`, never `Dto` or `Model`. Example: `OlxAttributeResponse`, not `OlxAttributeDto`.
- File lives in a `response/` sub-package inside the relevant `data/` package.

### Visibility
- Always `internal`. Response classes are an implementation detail of the data layer; nothing outside `data/` should reference them directly.

### Class kind
- Plain `class`, never `data class`. Response classes are deserialization targets, not value objects.

### Serialization
- Always annotate with `@Serializable`.
- Every field must have `@SerialName("json_key_name")` — even when the Kotlin name matches the JSON key exactly. This makes the contract explicit and rename-safe.

### Nullability and defaults
- Every field must be nullable (`?`). The backend cannot be trusted to always send every field.
- No default values in response classes. All defaults belong in the mapper.
- The mapper must handle every `null` case explicitly and supply appropriate fallback values.

### Mapper responsibilities
- Skip / filter out response items that are missing essential identity fields (e.g., a `code` that is `null`). Use `mapNotNull` for list transformations.
- Supply domain-layer defaults for missing optional fields (empty string, `false`, `emptyList()`, etc.).

### Custom serializers
- Avoid custom `KSerializer` implementations where the built-in behavior is sufficient.
- The OLX Ktor client is configured with `isLenient = true`, which means numeric JSON primitives are accepted where a `String` is expected — no custom serializer needed for mixed int/string id fields.

## Working Assumptions
- This repo is not a pure calorie app anymore; do not remove agile/seller functionality as “unused” without explicit instruction.
- `server` is currently minimal; most business logic is client/shared-side.
- `App.kt` should stay a rendering shell, not a dumping ground.
- When a change touches platform behavior, check for corresponding actual implementations in `androidMain`, `jvmMain`, `jsMain`, and `wasmJsMain`.
