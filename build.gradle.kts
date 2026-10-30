plugins {
    id("dev.prism")
}

group = "net.gmsgarcia.decor4all"
version = "2.0.0"

prism {
    metadata {
        modId = "decor4all"
        name = "Decor4All"
        description = "A decoration mod with furniture blocks that mostly have special features."
        license = "CC-BY-NC-SA-4.0"
        author("GmsGarcia#1553")
        archivesName = "decor4all-{mc}-{loader}"
        expand("homepage", "http://gmsgarcia.ga/decor")
        expand("sources", "https://github.com/GmsGarcia/decor4all")
    }

    version("1.21.11") {
        accessWidener("versions/1.21.11/common/src/main/resources/decor4all.classtweaker")
        javaVersion = 21
        minecraftVersions("1.21.11")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.141.6+1.21.11")
        }
        neoforge { loaderVersion = "21.11.45" }
    }

    version("26.1") {
        accessWidener("versions/26.1/common/src/main/resources/decor4all.classtweaker")
        javaVersion = 25
        minecraftVersions("26.1", "26.1.1", "26.1.2")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.155.3+26.1.2")
        }
        neoforge {
            loaderVersion = "26.1.2.112"
        }
    }

    version("26.2") {
        accessWidener("versions/26.2/common/src/main/resources/decor4all.classtweaker")
        javaVersion = 25
        minecraftVersions("26.2")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.161.0+26.2")
        }
        neoforge {
            loaderVersion = "26.2.0.88"
        }
    }

    version("26.3") {
        accessWidener("versions/26.3/common/src/main/resources/decor4all.classtweaker")
        javaVersion = 25
        minecraftVersions("26.3")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.161.0+26.3")
        }
        neoforge {
            loaderVersion = "26.3.0.26-beta"
        }
    }
}

// Puts `src/main/generated/resources` and the shared `shared/` assets on the jar for every version.
listOf("1.21.11", "26.1", "26.2", "26.3").forEach { mc ->
    val common = project(":$mc:common")

    common.plugins.withId("java") {
        val sourceSets = common.extensions.getByType<SourceSetContainer>()
        sourceSets.getByName("main")
            .resources.srcDir("src/main/generated/resources")
            .srcDir("../../../shared")

        sourceSets.create("generator") {
            java.srcDir("src/generator/java")
            compileClasspath += sourceSets.getByName("main").output + sourceSets.getByName("main").compileClasspath
            runtimeClasspath += sourceSets.getByName("main").output + sourceSets.getByName("main").runtimeClasspath
        }
    }
}

// Registers `generateResources` on every version's `common` project.
val generatorTargets = listOf(
    "1.21.11" to 21,
    "26.1" to 25,
    "26.2" to 25,
    "26.3" to 25,
)

generatorTargets.forEach { (mc, javaVersion) ->
    val common = project(":$mc:common")
    common.tasks.register<JavaExec>("generateResources") {
        group = "decor4all"
        description = "Regenerates src/main/generated/resources for Minecraft $mc."

        val generator = common.extensions.getByType<SourceSetContainer>()["generator"]
        val main = common.extensions.getByType<SourceSetContainer>()["main"]
        mainClass.set("net.gmsgarcia.decor4all.generator.ResourceGenerator")
        classpath = files(generator.runtimeClasspath, generator.output, main.output)
        dependsOn(common.tasks.named("generatorClasses"))

        workingDir = common.projectDir
        javaLauncher.set(
            common.extensions
                .getByType<JavaToolchainService>()
                .launcherFor { languageVersion.set(JavaLanguageVersion.of(javaVersion)) }
        )
    }
}
// Runs every version's generator in catalogue order.
val generateAllResources = tasks.register("generateAllResources") {
    group = "decor4all"
    description = "Regenerates resources for 1.21.11, 26.1, 26.2 and 26.3 in sequence."
    dependsOn(generatorTargets.map { (mc, _) -> "$mc:common:generateResources" })
}

gradle.projectsEvaluated {
    generatorTargets
        .map { (mc, _) -> project(":$mc:common").tasks.named("generateResources") }
        .zipWithNext()
        .forEach { (earlier, later) ->
            later.configure { mustRunAfter(earlier) }
        }
}
