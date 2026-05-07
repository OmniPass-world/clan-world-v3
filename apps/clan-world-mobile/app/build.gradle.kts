plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")
}

// Build-time URL config.
//
// We deliberately fall back from environment → Gradle project property → null.
// If neither is set the build fails loudly: a hardcoded fallback previously
// risked silently shipping a wrong/non-prod URL into release APKs.
//
// Local devs can keep the URLs in `~/.gradle/gradle.properties`:
//   clanWorldMapUrl=https://app.clan-world.com
//   clanWorldTerminalBaseUrl=https://cockpit.clan-world.com
// CI must pass the env vars explicitly.
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

// Optional stable signing key. See apps/kickstart-mobile/app/build.gradle.kts
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
    versionCode = 6
    versionName = "0.1.13"
    buildConfigField("String", "MAP_URL", "\"$mapUrl\"")
    buildConfigField("String", "TERMINAL_BASE_URL", "\"$terminalBaseUrl\"")
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
  // Compose BOM — pins all Compose lib versions to a tested set
  val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Compose core
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.foundation:foundation")
  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.runtime:runtime")
  debugImplementation("androidx.compose.ui:ui-tooling")

  // Activity / lifecycle
  implementation("androidx.activity:activity-compose:1.9.3")
  implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
  implementation("androidx.core:core-ktx:1.15.0")

  // Navigation
  implementation("androidx.navigation:navigation-compose:2.8.5")

  // WebView (modern APIs + safe browsing)
  implementation("androidx.webkit:webkit:1.12.1")

  // Solana Mobile Wallet Adapter — real signing
  implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.7")
}
