dependencies {
    api(project(":"))

    if (project.hasProperty("USE_SPIGOT_8")) {
        compileOnly(libs.spigot8)
    } else {
        compileOnly(libs.spigotLatest)
    }
}
