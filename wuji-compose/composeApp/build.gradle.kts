import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

version = "0.2.7"

kotlin {
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
    }
}

compose.desktop {
    application {
        mainClass = "com.wuji.app.Main_desktopKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "wuji"
            packageVersion = "1.0.0"
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
