plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":location:common"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
