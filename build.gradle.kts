buildscript { repositories { google(); mavenCentral() }; dependencies { classpath("com.android.tools:r8:9.1.56") } }
plugins {
 id("com.android.application") version "8.10.1" apply false
 id("com.android.library") version "8.10.1" apply false
 id("org.jetbrains.kotlin.android") version "2.3.20" apply false
 id("org.jetbrains.kotlin.multiplatform") version "2.3.20" apply false
 id("org.jetbrains.kotlin.plugin.compose") version "2.3.20" apply false
 id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20" apply false
 id("org.jetbrains.compose") version "1.10.3" apply false
}
