import java.util.Properties
import java.io.FileInputStream

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.2"
}

// Load version from version.properties (root)
val versionPropsFile = file("version.properties")
val versionProps = Properties()
versionProps.load(FileInputStream(versionPropsFile))
val pluginVersion = versionProps["version"] as String

// Load changelog from CHANGELOG.html
val changelogFile = file("CHANGELOG.html")
val changelogContent = if (changelogFile.exists()) {
    changelogFile.readText()
} else {
    "<h2>Version $pluginVersion</h2><p>See CHANGELOG.html for details.</p>"
}

group = "com.codinglegion"
version = pluginVersion

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

intellij {
    version.set("2020.1")
    type.set("IC") // IntelliJ Community Edition for maximum compatibility
    plugins.set(listOf("java", "Git4Idea"))
}

tasks {
    // Auto-copy version.properties and README.md from root to resources during build
    processResources {
        from(projectDir) {
            include("version.properties", "README.md")
            into(".")
        }
    }
    
    patchPluginXml {
        sinceBuild.set("201")
        untilBuild.set("999.*")
        
        // Load change notes from CHANGELOG.html
        changeNotes.set(changelogContent)
    }
    
    
    buildSearchableOptions {
        enabled = false
    }
    
    compileJava {
        options.encoding = "UTF-8"
    }
}

