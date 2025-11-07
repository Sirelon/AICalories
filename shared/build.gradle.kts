
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildKonfig)
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use(::load)
    }
}

fun resolveSecret(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        providers.gradleProperty(key).orNull
            ?: providers.environmentVariable(key).orNull
            ?: localProperties.getProperty(key)
    }

val supabaseUrl =
    resolveSecret("SUPABASE_URL", "supabase.url")
        ?: "https://example.supabase.co"

val supabaseKey =
    resolveSecret("SUPABASE_KEY", "supabase.key")
        ?: "public-anon-key"

val defaultEmail =
    resolveSecret("SUPABASE_DEFAULT_EMAIL", "supabase.default.email")
        ?: "test@sirelon.org"

val defaultPassword =
    resolveSecret("SUPABASE_DEFAULT_PASSWORD", "supabase.default.password")
        ?: "testMe"

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
            freeCompilerArgs.set(listOf("-Xannotation-default-target=param-property"))
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    js {
        browser()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.supabase.auth)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.functions)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.sirelon.aicalories.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

buildkonfig {
    packageName = "com.sirelon.aicalories.supabase"
    objectName = "SupabaseConfig"

    defaultConfigs {
        buildConfigField(STRING, "SUPABASE_URL", supabaseUrl)
        buildConfigField(STRING, "SUPABASE_KEY", supabaseKey)
        buildConfigField(STRING, "SUPABASE_DEFAULT_EMAIL", defaultEmail)
        buildConfigField(STRING, "SUPABASE_DEFAULT_PASSWORD", defaultPassword)
    }
}
