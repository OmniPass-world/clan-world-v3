plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

// Build-time URL config.
//
// We deliberately fall back from environment → Gradle project property → null.
// If neither is set the build fails loudly: previously the env-var-only path
// silently shipped the dev Convex URL into release APKs.
//
// Local devs can keep the dev URL in `~/.gradle/gradle.properties`:
//   clanworldConvexUrl=https://valuable-kudu-985.convex.cloud
//   kickstartHomeUrl=https://kickstart.easya.io
// CI must pass the env var explicitly.
fun resolveBuildConfigUrl(envName: String, propName: String): String {
  val envValue = System.getenv(envName)?.takeIf { it.isNotBlank() }
  if (envValue != null) return envValue
  val propValue = (project.findProperty(propName) as? String)?.takeIf { it.isNotBlank() }
  if (propValue != null) return propValue
  error(
    "$envName must be set at build time (env var or Gradle property `$propName`). " +
      "Refusing to fall back to a hardcoded URL — that path silently shipped the dev " +
      "backend in past releases.",
  )
}

val convexUrl = resolveBuildConfigUrl("CLANWORLD_CONVEX_URL", "clanworldConvexUrl")
val homeUrl = resolveBuildConfigUrl("KICKSTART_HOME_URL", "kickstartHomeUrl")

// Optional stable signing key. When all four env vars are set (CI builds), debug
// APKs are signed with the same key across releases so users can upgrade in
// place. When unset (local dev), Gradle falls back to its auto-generated debug
// keystore — fine for `./gradlew assembleDebug` on a workstation but each
// machine produces a different cert.
val releaseKeystorePath: String? = System.getenv("RELEASE_KEYSTORE_PATH")?.takeIf { it.isNotBlank() }
val releaseKeystorePassword: String? = System.getenv("RELEASE_KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = System.getenv("RELEASE_KEY_ALIAS")
val releaseKeyPassword: String? = System.getenv("RELEASE_KEY_PASSWORD")
val hasStableSigning = releaseKeystorePath != null &&
  !releaseKeystorePassword.isNullOrBlank() &&
  !releaseKeyAlias.isNullOrBlank() &&
  !releaseKeyPassword.isNullOrBlank()

android {
  namespace = "io.easya.kickstart"
  compileSdk = 35

  defaultConfig {
    applicationId = "io.easya.kickstart"
    minSdk = 26
    targetSdk = 35
    versionCode = 10
    versionName = "0.1.10"
    buildConfigField("String", "CONVEX_URL", "\"$convexUrl\"")
    buildConfigField("String", "HOME_URL", "\"$homeUrl\"")
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
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }
}
