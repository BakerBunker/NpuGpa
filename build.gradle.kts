// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
//        maven {
//            url=uri("https://maven.aliyun.com/repository/central")
//        }
//        google{
//            url=uri("https://maven.aliyun.com/repository/google")
//        }
//        mavenCentral{
//            url=uri("https://maven.aliyun.com/repository/central")
//        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}