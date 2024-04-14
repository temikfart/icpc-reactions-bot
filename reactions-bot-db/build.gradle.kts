plugins {
    id("bot.library-conventions")
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/icpc/live-v3")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation(libs.icpclive.cds.full)
    implementation(libs.icpclive.cds.utils)
    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)
}
