pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { google(); mavenCentral(); maven("https://jitpack.io") } }
rootProject.name = "StreamXComicSkin"
include(":app", ":shared", ":sharedUi")
project(":shared").projectDir = file("../../../shared")
project(":sharedUi").projectDir = file("../../../sharedUi")
