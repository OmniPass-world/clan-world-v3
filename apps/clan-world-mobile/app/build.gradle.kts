plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
}

// ─────────────────────────────────────────────────────────────────────────
// Build-time URL config.
//
// MAP_URL and TERMINAL_BASE_URL: required at build time. We deliberately
// fall back from env → Gradle property → hard fail. A hardcoded fallback
// previously risked silently shipping the wrong backend into release APKs.
//
// CONVEX_URL: optional with a sensible default (the V3 Convex deployment).
// Falling back is safe here because the default points to the public read
// endpoint — there's no auth or write surface that could leak.
//
// Local devs can keep these in `~/.gradle/gradle.properties`:
//   clanWorldMapUrl=https://app.clan-world.com
//   clanWorldTerminalBaseUrl=https://cockpit.clan-world.com
// ─────────────────────────────────────────────────────────────────────────

fun resolveBuildConfigUrl(envName: String, propName: String): String {
  val envValue = System.getenv(envName)?.takeIf { it.isNotBlank() }
  if (envValue != null) return envValue
  val propValue = (project.findProperty(propName) as? String)?.takeIf { it.isNotBlank() }
  if (propValue != null) return propValue
  error(
    "$envName must be set at build time (env var or Gradle property `$propName`). " +
      "Refusing to fall back to a hardcoded URL — that path silently shipped wrong " +
      "backends in past releases.",
  )
}

val mapUrl = resolveBuildConfigUrl("CLAN_WORLD_MAP_URL", "clanWorldMapUrl")
val terminalBaseUrl = resolveBuildConfigUrl("CLAN_WORLD_TERMINAL_BASE_URL", "clanWorldTerminalBaseUrl")
val convexUrl = providers.environmentVariable("CLAN_WORLD_CONVEX_URL")
  .orElse("https://valuable-kudu-985.convex.cloud")

// for details — both apps share the same secret names so one CI step can
// provision both.
val releaseKeystorePath: String? = System.getenv("RELEASE_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
val releaseKeystorePassword: String? = System.getenv("RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("RELEASE_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("RELEASE_KEY_PASSWORD")
val hasStableSigning = releaseKeystorePath != null &&
  !releaseKeystorePassword.isNullOrBlank() &&
  !releaseKeyAlias.isNullOrBlank() &&
  !releaseKeyPassword.isNullOrBlank()

android {
  namespace = "world.clan.app"
  compileSdk = 35

  defaultConfig {
    applicationId = "world.clan.app"
    minSdk = 26
    targetSdk = 35
    versionCode = 7
    versionName = "0.1.14"
    buildConfigField("String", "MAP_URL", "\"$mapUrl\"")
    buildConfigField("String", "TERMINAL_BASE_URL", "\"$terminalBaseUrl\"")
    buildConfigField("String", "CONVEX_URL", "\"${convexUrl.get()}\"")
    vectorDrawables { useSupportLibrary = true }
  }

  if (hasStableSigning) {
    signingConfigs {
      create("stable") {
        storeFile = file(releaseKeystorePath!!)
        storePassword = releaseKeystorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    debug {
      if (hasStableSigning) {
        signingConfig = signingConfigs.getByName("stable")
      }
    }
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }
  // Kotlin 2.0 routes Compose through the kotlin.plugin.compose plugin —
  // do NOT add `composeOptions { kotlinCompilerExtensionVersion = … }`.

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

dependencies {
  // ── Core / lifecycle ─────────────────────────────────────────────────
  implementation("androidx.core:core-ktx:1.15.0")
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

  // ── Compose BOM — single source of truth for Compose artifact versions ─
  val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // ── Compose UI + Material3 ───────────────────────────────────────────
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.runtime:runtime")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material:material-icons-extended")

  // ── Navigation ───────────────────────────────────────────────────────
  implementation("androidx.navigation:navigation-compose:2.8.5")

  // ── WebView (cockpit's MapWebView + slice 2 cockpit URL surface) ─────
  implementation("androidx.webkit:webkit:1.12.1")

  // ── Coroutines ───────────────────────────────────────────────────────
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

  // ── JSON serialization (Convex client) ───────────────────────────────
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

  // ── Networking — single shared OkHttp instance ───────────────────────
  implementation("com.squareup.okhttp3:okhttp:4.12.0")

  // ── Encrypted SharedPreferences (session token) ──────────────────────
  implementation("androidx.security:security-crypto:1.1.0-alpha06")

  // ── Image loading (Coil) ─────────────────────────────────────────────
  implementation("io.coil-kt:coil-compose:2.7.0")

  // ── Solana Mobile Wallet Adapter ─────────────────────────────────────
  // Pinned to 2.0.7. The 2.1.0 release pulls androidx.core:core-ktx:1.17.0
  // transitively, which requires AGP 8.9.1 + compileSdk 36. We're on AGP
  // 8.7.3 / compileSdk 35. The 2.0.x line is API-identical for our usage
  // (verified against the v2.0.5 + v2.0.7 tags).
  implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.7")

  // ── Tests ────────────────────────────────────────────────────────────
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
