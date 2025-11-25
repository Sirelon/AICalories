
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val composeOptInAnnotations = listOf(
    "androidx.compose.foundation.layout.ExperimentalLayoutApi",
    "androidx.compose.material3.ExperimentalMaterial3Api",
    "androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
    "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    "androidx.compose.ui.ExperimentalComposeUiApi",
    "com.mohamedrejeb.calf.core.InternalCalfApi",
    "com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi",
    "kotlin.time.ExperimentalTime",
    "kotlin.uuid.ExperimentalUuidApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.cinterop.ExperimentalForeignApi",
)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            binaryOptions["bundleId"] = "com.sirelon.aicalories"
        }
    }
    
    jvm()
    
    js {
        browser()
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    
    sourceSets {
        all {
            languageSettings.apply {
                composeOptInAnnotations.forEach { optIn(it) }
            }
        }
        val jsWasmMain by creating {
            dependsOn(getByName("commonMain"))
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.compose.auth.ui)
            implementation(libs.supabase.coil3.integration)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.preview)
            implementation(libs.material3)
            implementation(libs.material3.adaptive)
            implementation(libs.material3.adaptive.navigation.suite)
            implementation(libs.material.icons.extended)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodel)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.navigation3)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(projects.shared)
            implementation(libs.androidx.navigation3.runtime)

            implementation(libs.navigation3.ui)
//
//            implementation(libs.androidx.navigation3.ui)
//            implementation(libs.androidx.navigation3.viewmodel)

            implementation(libs.calf.filepicker)
            implementation(libs.calf.filepicker.coil)
            implementation(libs.calf.permissions)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.java)
            implementation(libs.kotlinx.coroutines.swing)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        getByName("jsMain").dependsOn(jsWasmMain)
        getByName("wasmJsMain").dependsOn(jsWasmMain)
    }
}

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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.sirelon.aicalories.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.sirelon.aicalories"
            packageVersion = "1.0.0"
        }
    }
}
