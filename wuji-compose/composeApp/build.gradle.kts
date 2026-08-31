import com.android.build.api.variant.VariantOutput
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
}

version = "0.0.02"

kotlin {
    // Android 目标 - 产出 APK 安装包
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    // 当前以 Desktop(JVM)为目标进行编译与运行,commonMain 代码保持 Android-ready
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // 网络
                implementation(libs.ktor.core)
                implementation(libs.ktor.okhttp)
                implementation(libs.ktor.content)
                implementation(libs.ktor.json)
                implementation(libs.ktor.logging)
                implementation(libs.ktor.auth)

                // 依赖注入
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.viewmodel)

                // 导航
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.tab.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.koin)

                // HTML 解析(源引擎核心)
                implementation(libs.ksoup)

                // 图片加载
                implementation(libs.coil.compose)
                implementation(libs.coil.network)

                // 持久化
                implementation(libs.settings)
                implementation(libs.settings.noarg)

                // 日志
                implementation(libs.napier)

                // kotlinx
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.uri)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.filePicker)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
            }
        }
    }
}

/**
 * Android 构建配置 - 对齐原 Tauri 在移动端的能力,产出 APK 安装包。
 * minSdk 24 覆盖主流设备;targetSdk 取 AGP 默认推荐值。
 */
android {
    namespace = "com.wuji.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wuji.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "0.0.02"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // 对齐桌面端:暂不启用 R8/资源压缩,确保骨架稳定可运行
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // =============== 按 ABI 拆分 APK (arm64-v8a / armeabi-v7a / x86_64 / x86 + universal) ===============
    // 每个架构单独一个 APK,体积更小;同时保留通用 universal 包兜底。
    // versionCode 按架构加偏移,保证覆盖升级时每个架构独立单调递增 (Google Play 商店要求)。
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
    // AGP 8.x 通过 VariantOutput.getFilter 公开 API 读取 ABI 过滤器计算 versionCode 偏移
    // (universal APK 没有 ABI filter,versionCode = base)
    applicationVariants.all {
        val baseVersionCode = defaultConfig.versionCode!!
        outputs.forEach { output ->
            val impl = output as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            val abi = impl.getFilter(VariantOutput.FilterType.ABI)
            val abiOffset = when (abi) {
                "armeabi-v7a" -> 10
                "arm64-v8a"   -> 20
                "x86"         -> 30
                "x86_64"      -> 40
                null /* universal */ -> 0
                else -> 0
            }
            impl.versionCodeOverride = baseVersionCode * 100 + abiOffset
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.wuji.app.Main_desktopKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "wuji"
            packageVersion = "0.0.02"
            description = "无极 - 跨平台资源聚合浏览器"
            vendor = "wuji"
            windows {
                menuGroup = "wuji"
                upgradeUuid = "8f6b2c1a-3d4e-4f5a-9b6c-7d8e9f0a1b2c"
            }
            macOS {
                bundleID = "com.wuji.app"
            }
            linux {
                packageName = "wuji"
            }
        }

        buildTypes.release.proguard {
            isEnabled = false
        }
    }
}
