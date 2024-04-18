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
        name = "jitpack"
        url = uri("https://jitpack.io")
        content {
            // This limits this repo to this group
            includeGroup("io.github.kotlin-telegram-bot.kotlin-telegram-bot")
        }
    }
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
    mainClass.set("org.icpclive.reactionsbot.ReactionsBotKt")
}

tasks.runTask {
    val args = mutableListOf<String>()
    project.properties["live.dev.token"]?.let { args += listOf("--token", it.toString()) }
    project.properties["live.dev.chat"]?.let { args += listOf("--chat", it.toString()) }
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
    implementation(libs.telegram.bot)

    implementation(project(":reactions-bot-db"))
    implementation(libs.mongo.driver)
    implementation(libs.mongo.bson)
}
