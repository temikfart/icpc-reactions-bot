plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradleplugin.kotlin.jvm)
    implementation(libs.gradleplugin.kotlin.serialization)
    implementation(libs.gradleplugin.shadow)
}
