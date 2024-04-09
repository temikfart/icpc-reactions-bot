import org.gradle.kotlin.dsl.run as runTask

plugins {
    id("bot.app-conventions")
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

application {
    mainClass.set("org.icpclive.reactionsbot.loader.ReactionsBotLoaderKt")
}

tasks.runTask {
    val args = mutableListOf<String>()
    project.properties["live.dev.video"]?.let { args += listOf("--video", it.toString()) }
    project.properties["live.dev.config"]?.let { args += listOf("--config-directory", it.toString()) }
    this.args = args
}

dependencies {
    implementation(libs.cli)
    implementation(libs.db.sqlite)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    implementation("org.icpclive:org.icpclive.cds.full:0.11")
    implementation("org.icpclive:org.icpclive.cds.utils:0.11")
}
