rootProject.name = "reactions-bot"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {}
    }
}

include(":reactions-bot")
include(":reactions-bot-db")
include(":reactions-bot-loader")
