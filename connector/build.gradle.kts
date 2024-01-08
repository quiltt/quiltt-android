plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "app.quiltt.connector"
    compileSdk = 33

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

publishing {
    publications {
        register<MavenPublication>("connector") {
            groupId = "app.quiltt"
            artifactId = "connector"
            version = "0.0.0"

            pom {
                name.set("Quiltt Connector")
                description.set("Quiltt Connector Android SDK")
                url.set("https://www.quiltt.dev/guides/connector/android")

                organization {
                    name.set("Quiltt, Inc.")
                    url.set("https://www.quiltt.io/")
                }

                licenses {
                    license {
                        name.set("The MIT License (MIT)")
                        url.set("http://www.opensource.org/licenses/mit-license.php")
                    }
                }

                developers {
                    developer {
                        id.set("tom-quiltt")
                        name.set("Tom Lee")
                        email.set("tom@quiltt.io")
                    }
                }

                scm {
                    connection.set("scm:git:github.com/quiltt/quiltt-android.git")
                    developerConnection.set("scm:git:ssh://github.com/quiltt/quiltt-android.git")
                    url.set("https://github.com/quiltt/quiltt-android/tree/main/connector")
                }
            }

            artifact("$buildDir/outputs/aar/connector-release.aar")
        } // app.quiltt:quiltt-connector:0.0.0
    }
    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

// Set the GPG key to sign artifacts with:
// signing.gnupg.keyName=
// signing.gnupg.passphrase=
signing {
    useGpgCmd()
    sign(publishing.publications["connector"])
}