// Plugin versions come from gradle/libs.versions.toml and are applied per module.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// ./build is the dev-CLI shim script, so the root project's output cannot also
// be called build/ — Gradle fails with "could not create directory" rather than
// naming the collision. Only the root project moves; :app keeps app/build.
layout.buildDirectory.set(layout.projectDirectory.dir(".build"))
