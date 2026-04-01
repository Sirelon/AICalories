# AGENTS

## Repo Overview
- Kotlin Multiplatform app with modules: `composeApp`, `androidApp`, `shared`, and `server`.
- `composeApp` contains the shared Compose UI and desktop/web targets.
- `shared` contains shared domain and config code, including BuildKonfig-backed secrets.
- `androidApp` is the Android entrypoint.
- `server` is a Ktor JVM app.

## Verified Commands
- Android debug build: `./gradlew :composeApp:assembleDebug`
- Desktop run: `./gradlew :composeApp:run`
- Server run: `./gradlew :server:run`
- Web dev run (Wasm): `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
- Web dev run (JS): `./gradlew :composeApp:jsBrowserDevelopmentRun`
- iOS app: open `iosApp` in Xcode and run from there

## Repo-Specific Workflow Notes
- `composeApp/build.gradle.kts` registers `copyComposeResourcesToAndroidAssets` and wires it into Android `preBuild`. Do not remove it casually; it works around Compose resource generation issues for the Android KMP library target.
- `composeApp/build.gradle.kts` also disables `copyAndroidMainComposeResourcesToAndroidAssets` when present to avoid known build failures.
- Runtime secrets for Supabase and OLX are resolved in `shared/build.gradle.kts` from Gradle properties, environment variables, or `local.properties`.
- Supported secret keys:
  - `SUPABASE_URL` or `supabase.url`
  - `SUPABASE_KEY` or `supabase.key`
  - `SUPABASE_DEFAULT_EMAIL` or `supabase.default.email`
  - `SUPABASE_DEFAULT_PASSWORD` or `supabase.default.password`
  - `OLX_CLIENT_ID` or `olx.client.id`
  - `OLX_CLIENT_SECRET` or `olx.client.secret`
  - `OLX_SCOPE` or `olx.scope`
  - `OLX_AUTH_BASE_URL` or `olx.auth.base.url`
  - `OLX_API_BASE_URL` or `olx.api.base.url`
  - `OLX_REDIRECT_URI` or `olx.redirect.uri`

## TODO
- Confirm the preferred test and lint commands once Gradle task listing can run in this environment; wrapper task inspection was blocked by sandbox access to `~/.gradle`.
