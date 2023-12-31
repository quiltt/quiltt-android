plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "app.quiltt.connector"
    compileSdk = 26

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("quiltt-connector") {
                groupId = "app.quiltt"
                artifactId = "quiltt-connector"
                version = "0.0.0"

                artifact("$buildDir/outputs/aar/connector-release.aar")
            } // app.quiltt:quiltt-connector:0.0.0
        }
        repositories {
            maven {
                name = "quiltt-connector-kotlin"
                url = uri("${project.buildDir}/quiltt-connector-kotlin")
            }
        }
    }
}
