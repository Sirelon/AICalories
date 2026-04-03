# AGENTS

## Repo Overview
Kotlin Multiplatform (KMP) app targeting Android, iOS, Desktop, and Web.

- `composeApp` — shared Compose Multiplatform UI; targets Android, Desktop (JVM), Web (Wasm/JS)
- `shared` — shared domain logic, config, and BuildKonfig-backed secrets; targets all platforms
- `androidApp` — Android entrypoint
- `iosApp` — iOS entrypoint (Xcode project)
- `server` — Ktor JVM backend

### Feature modules (under `composeApp/src/commonMain/.../features/`)
- `agile` — agile estimation tools (capacity planning, team management)
- `analyze` — food analysis (AI-powered calorie detection)
- `history` — meal/calorie history
- `seller` — seller flow with OLX ad generation (`ad/`, `auth/`, `onboarding/`)
- `media`, `datagenerator`, `common` — shared utilities

## Build & Run Commands
- Android debug build: `./gradlew :composeApp:assembleDebug`
- Desktop run: `./gradlew :composeApp:run`
- Server run: `./gradlew :server:run`
- Web (Wasm): `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- Web (JS): `./gradlew :composeApp:jsBrowserDevelopmentRun`
- iOS: open `iosApp/` in Xcode and run from there

## Test & Lint Commands
- Run all tests: `./gradlew allTests`
- Android lint: `./gradlew lint`
- JS tests: `./gradlew jsTest`
- JVM tests: `./gradlew jvmTest`
- iOS simulator tests: `./gradlew iosSimulatorArm64Test`

## Build Notes
- `composeApp/build.gradle.kts` registers `copyComposeResourcesToAndroidAssets` wired into Android `preBuild` — do not remove; it works around Compose resource generation issues for the Android KMP library target.
- `copyAndroidMainComposeResourcesToAndroidAssets` is disabled when present to avoid known build failures.
- Gradle JVM heap is set to `-Xmx4g` in `gradle.properties`.

## Secrets Configuration
Resolved in `shared/build.gradle.kts` from Gradle properties, environment variables, or `local.properties`:

| Key (env / property) | Purpose |
|---|---|
| `SUPABASE_URL` / `supabase.url` | Supabase project URL |
| `SUPABASE_KEY` / `supabase.key` | Supabase anon key |
| `SUPABASE_DEFAULT_EMAIL` / `supabase.default.email` | Default test account |
| `SUPABASE_DEFAULT_PASSWORD` / `supabase.default.password` | Default test password |
| `OLX_CLIENT_ID` / `olx.client.id` | OLX OAuth client ID |
| `OLX_CLIENT_SECRET` / `olx.client.secret` | OLX OAuth secret |
| `OLX_SCOPE` / `olx.scope` | OLX OAuth scope |
| `OLX_AUTH_BASE_URL` / `olx.auth.base.url` | OLX auth base URL |
| `OLX_API_BASE_URL` / `olx.api.base.url` | OLX API base URL |
| `OLX_REDIRECT_URI` / `olx.redirect.uri` | OLX redirect URI |

## Library Documentation & Version Lookup

When checking Android/Google library versions, APIs, or usage:
- **Always use the `mcp__google-dev-knowledge__search_documents` / `mcp__google-dev-knowledge__get_documents` MCP tools** instead of relying on training-data knowledge or web search.
- This applies to any Google/Android library: Jetpack Compose, AndroidX, Material, Ktor Android, Google Play services, Firebase, etc.
- Use it to verify correct API signatures, version compatibility, and migration guides before writing or recommending code.

## UI & Theming Conventions
- Prefer `AppTheme.typography` and `AppTheme.colors` over `MaterialTheme.typography` / `MaterialTheme.colorScheme`.
- Use `AppDimens` tokens instead of hardcoded `dp` values in shared UI. Keep raw values only for layout breakpoints (e.g. `720.dp`).
- Add tokens to `AppDimens.Size` only when needed; keep `xlN` naming in ascending size order.
- Prefer the nearest existing `AppDimens` token; add a new token only when the exact value matters (e.g. hero height).
- Design system components live in `composeApp/src/commonMain/.../designsystem/`.
