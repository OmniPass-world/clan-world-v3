plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

// Build-time URL config.
//
// We deliberately fall back from environment → Gradle project property → null.
// If neither is set the build fails loudly: a hardcoded fallback previously
// risked silently shipping a wrong/non-prod URL into release APKs.
//
// Local devs can keep the URL in `~/.gradle/gradle.properties`:
//   clanWorldHomeUrl=https://clan-world.com
// CI must pass the env var explicitly.
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

val homeUrl = resolveBuildConfigUrl("CLAN_WORLD_HOME_URL", "clanWorldHomeUrl")

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
    versionCode = 3
    versionName = "0.1.10"
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
