pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { google(); mavenCentral(); maven("https://jitpack.io") } }
rootProject.name = "StreamXLooperSkin"
include(":app", ":shared", ":sharedUi")
project(":shared").projectDir = file("../../../shared")
project(":sharedUi").projectDir = file("../../../sharedUi")
