import org.gradle.kotlin.dsl.run as runTask
import io.github.cdimascio.dotenv.Dotenv

buildscript {
    dependencies {
        classpath("io.github.cdimascio:dotenv-kotlin:6.4.1")
    }
}

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

    val dotenv = Dotenv.configure()
        .directory("${rootDir.path}/docker/reactions-bot")
        .filename(".env")
        .ignoreIfMissing()
        .load()

    dotenv.entries().forEach {
        environment(it.key, it.value)
    }
}

dependencies {
    implementation(libs.dotenv)

    implementation(libs.cli)
    implementation(libs.icpclive.cds.full)
    implementation(libs.icpclive.cds.utils)

    implementation(project(":reactions-bot-db"))
    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)
}
