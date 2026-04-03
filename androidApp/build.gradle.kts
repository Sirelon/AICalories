plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

val composeAppProject = project(":composeApp")
android {
    namespace = "com.sirelon.aicalories"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sirelon.aicalories"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Workaround: Include compose resources from composeApp as Android assets.
// The Compose Resources plugin doesn't properly wire assets with androidKotlinMultiplatformLibrary.
androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addStaticSourceDirectory(
            composeAppProject.layout.buildDirectory.dir("generated/compose/androidAssets").get().asFile.absolutePath
        )
    }
}

tasks.matching { it.name.contains("MergeAssets") || it.name.contains("mergeAssets") }.configureEach {
    dependsOn(composeAppProject.tasks.named("copyComposeResourcesToAndroidAssets"))
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
}
