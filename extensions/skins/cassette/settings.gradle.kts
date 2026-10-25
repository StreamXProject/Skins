pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositories { google(); mavenCentral(); maven("https://jitpack.io") } }
rootProject.name = "StreamXCassetteSkin"
include(":app", ":shared", ":sharedUi", ":skinCommon")
project(":shared").projectDir = file("../../../shared")
project(":sharedUi").projectDir = file("../../../sharedUi")
project(":skinCommon").projectDir = file("../common")
