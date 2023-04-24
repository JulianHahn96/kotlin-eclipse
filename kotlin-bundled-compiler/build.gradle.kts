import com.intellij.buildsupport.dependencies.PackageListFromSimpleFile
import com.intellij.buildsupport.resolve.http.HttpArtifact
import com.intellij.buildsupport.resolve.http.HttpArtifactsResolver
import com.intellij.buildsupport.resolve.http.idea.IntellijIdeaArtifactsResolver
import com.intellij.buildsupport.utils.FileUtils

apply(plugin = "base")

// constants
val teamcityBaseUrl ="https://teamcity.jetbrains.com"
val ideaSdkUrl = "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea"

// properties that might/should be modifiable

//val kotlinCompilerTcBuildId: String = project.findProperty("kotlinCompilerTcBuildId") as String? ?: "3546752"
val kotlinPluginUpdateId = project.findProperty("kotlinPluginUpdateId") as String? ?: "314827" // Kotlin Plugin 1.8.20 for Idea 2022.2

val kotlinCompilerVersion: String = project.findProperty("kotlinCompilerVersion") as String? ?: "1.8.20"
val kotlinxVersion: String = project.findProperty("kolinxVersion") as String? ?: "1.6.3"
val tcArtifactsPath: String = project.findProperty("tcArtifactsPath") as String? ?: ""
val ideaVersion: String = project.findProperty("ideaVersion") as String? ?: "231.8109.175" //Idea 2023.1
val kotlinIdeaCompatibleVersionMinor: String = project.findProperty("kotlinIdeaCompatibleVersionMinor") as String? ?: "2022.2"
val ignoreSources: Boolean = true//project.hasProperty("ignoreSources")

//directories
val testDataDir = file("${projectDir.parentFile}/kotlin-eclipse-ui-test/common_testData")
//TODO later refactor to the proper project dir
val testModuleLibDir = file("${projectDir.parentFile}/kotlin-eclipse-ui-test/lib")
//TODO later refactor to the proper project dir

val downloadDirName = "downloads"

val teamCityWorkingDir = project.findProperty("teamcity.buildsupport.workingDir")
val libDir = if (teamCityWorkingDir != null) file("$teamCityWorkingDir/lib") else file("lib")

val localTCArtifacts: Boolean = tcArtifactsPath.isNotBlank()
val downloadDir = if(localTCArtifacts) file(tcArtifactsPath) else file("$libDir/$downloadDirName")

/*val tcArtifactsResolver = KotlinCompilerTCArtifactsResolver(teamcityBaseUrl,
        project.hasProperty("lastSuccessfulBuild"),
        kotlinCompilerTcBuildId,
        kotlinCompilerVersion,
        kotlinIdeaCompatibleVersionMinor)*/

HttpArtifactsResolver.getProxyProps()["https.proxyHost"] = project.findProperty("https.proxyHost") ?: System.getProperty("https.proxyHost")
HttpArtifactsResolver.getProxyProps()["https.proxyPort"] = project.findProperty("https.proxyPort") ?: System.getProperty("https.proxyPort")
HttpArtifactsResolver.getProxyProps()["https.proxyUser"] = project.findProperty("https.proxyUser") ?: System.getProperty("https.proxyUser")
HttpArtifactsResolver.getProxyProps()["https.proxyPassword"] = project.findProperty("https.proxyPassword") ?: System.getProperty("https.proxyPassword")

val ideaArtifactsResolver = IntellijIdeaArtifactsResolver(ideaSdkUrl, ideaVersion)
val kotlinPluginArtifactsResolver = HttpArtifactsResolver("https://plugins.jetbrains.com")

val tempKotlinHttpArtifact = HttpArtifact("plugin/download?rel=true&updateId=$kotlinPluginUpdateId")

tasks.withType<Wrapper> {
    gradleVersion = "5.5.1"
}

val testFrameworkDependencies by configurations.creating
val kotlinxLibraries by configurations.creating

dependencies {
    testFrameworkDependencies("com.google.code.gson:gson:2.3.1")
    kotlinxLibraries("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion") { isTransitive = false }
    kotlinxLibraries("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion") { isTransitive = false }
}

repositories {
    mavenCentral()
}

tasks.named<Delete>("clean") {
    doLast {
        FileUtils.cleanDir(testDataDir)
        FileUtils.cleanDir(testModuleLibDir)
        FileUtils.cleanDirExceptSubDirName(libDir, downloadDirName)
    }
}

val deleteLibrariesFromLibFolder by tasks.registering {
    doFirst {
        libDir.listFiles()?.filter { it.isFile }?.forEach { it.deleteRecursively() }
    }
}

val downloadTestData by tasks.registering {
    val locallyDownloadedTestDataFile by extra {
        if(localTCArtifacts){
            file("$tcArtifactsPath/kotlin-test-data.zip")
        } else {
            file("$testDataDir/kotlin-test-data.zip")
        }
    }

    doLast {
        //TODO can we get the test data from somewhere?
        if (!localTCArtifacts && !locallyDownloadedTestDataFile.exists()) {
            //tcArtifactsResolver.downloadTo(tcArtifactsResolver.KOTLIN_TEST_DATA_ZIP, locallyDownloadedTestDataFile)
        }

        /*copy {
            from(zipTree(locallyDownloadedTestDataFile))
            into(testDataDir)
        }*/
    }
}

val downloadTestFrameworkDependencies by tasks.registering(Copy::class) {
    from(testFrameworkDependencies)
    into(testModuleLibDir)
}

val downloadKotlinCompilerPluginAndExtractSelectedJars by tasks.registering {
    dependsOn(deleteLibrariesFromLibFolder)

    val kotlinDownloadDir = file("$downloadDir/kotlin-$kotlinCompilerVersion/$kotlinIdeaCompatibleVersionMinor")
    val locallyDownloadedCompilerFile by extra {
        file(kotlinDownloadDir).listFiles()?.firstOrNull { it.name.startsWith("kotlin-plugin-") }
                ?: file("$kotlinDownloadDir/kotlin-plugin.zip")
    }

    doLast {
        if (!localTCArtifacts && !locallyDownloadedCompilerFile.exists()) {
            kotlinPluginArtifactsResolver.downloadTo(tempKotlinHttpArtifact, locallyDownloadedCompilerFile)
            //tcArtifactsResolver.downloadTo(tcArtifactsResolver.KOTLIN_PLUGIN_ZIP, locallyDownloadedCompilerFile)
        }

        copy {
            from(zipTree(locallyDownloadedCompilerFile))

            setIncludes(setOf("Kotlin/lib/kotlin-plugin.jar",
                    "Kotlin/kotlinc/lib/kotlin-compiler.jar",
                    "Kotlin/kotlinc/lib/kotlin-stdlib.jar",
                    "Kotlin/kotlinc/lib/kotlin-reflect.jar",
                    "Kotlin/kotlinc/lib/kotlin-script-runtime.jar",
                    "Kotlin/kotlinc/lib/kotlin-scripting-compiler.jar",
                    "Kotlin/kotlinc/lib/kotlin-scripting-common.jar",
                    "Kotlin/kotlinc/lib/kotlin-scripting-jvm.jar",
                    "Kotlin/kotlinc/lib/kotlin-scripting-compiler-impl.jar",
                    "Kotlin/kotlinc/lib/kotlin-jdk-annotations.jar",
                    "Kotlin/kotlinc/lib/kotlin-stdlib-sources.jar",
                    "Kotlin/kotlinc/lib/allopen-compiler-plugin.jar",
                    "Kotlin/kotlinc/lib/noarg-compiler-plugin.jar",
                    "Kotlin/kotlinc/lib/sam-with-receiver-compiler-plugin.jar",
                    "Kotlin/kotlinc/lib/annotations-13.0.jar"))

            includeEmptyDirs = false
            into(libDir)

            // flatten + rename
            eachFile {
                this.relativePath = RelativePath(true, this.name)
            }
        }
    }
}

val extractPackagesFromPlugin by tasks.registering(Jar::class) {
    dependsOn(downloadKotlinCompilerPluginAndExtractSelectedJars)
    dependsOn(downloadKotlinxLibraries)

    from(zipTree("$libDir/kotlin-plugin.jar"))
    destinationDirectory.set(libDir)
    archiveFileName.set("kotlin-plugin-parts.jar")
    include("**")
    exclude("com/intellij/util/**")

    doLast {
        file("$libDir/kotlin-plugin.jar").delete()
    }
}

val extractPackagesFromKTCompiler by tasks.registering(Jar::class) {
    dependsOn(downloadKotlinCompilerPluginAndExtractSelectedJars)
    dependsOn(downloadKotlinxLibraries)

    from(zipTree("$libDir/kotlin-compiler.jar"))
    destinationDirectory.set(libDir)
    archiveFileName.set("kotlin-compiler-tmp.jar")
    include("**")
    exclude("com/intellij/openapi/util/text/**")
    exclude("com/intellij/util/containers/MultiMap*")
    exclude("com/intellij/util/containers/ContainerUtil.class")

    doLast {
        file("$libDir/kotlin-compiler.jar").delete()
        file("$libDir/kotlin-compiler-tmp.jar").renameTo(file("$libDir/kotlin-compiler.jar"))
    }
}

val downloadIdeaDistributionZipAndExtractSelectedJars by tasks.registering {
    dependsOn(deleteLibrariesFromLibFolder)
    val ideaDownloadDir = file("$downloadDir/idea-$ideaVersion")
    val locallyDownloadedIdeaZipFile by extra { file("$ideaDownloadDir/ideaIC.zip") }
    val chosenJars by extra { setOf(
            "util",
            "util_rt",
            "idea_rt",
            "app"
        )
    }

    doLast {
        if(!locallyDownloadedIdeaZipFile.exists()) {
            ideaArtifactsResolver.downloadTo(ideaArtifactsResolver.IDEA_IC_ZIP, locallyDownloadedIdeaZipFile)
        }
        copy {
            from(zipTree(locallyDownloadedIdeaZipFile))

            setIncludes(chosenJars.map { "lib/$it.jar" }.toSet())

            includeEmptyDirs = false

            into(libDir)

// flatten the files
            eachFile {
                this.relativePath = RelativePath(true, this.name)
            }
        }
    }
}

val extractSelectedFilesFromIdeaJars by tasks.registering {
    dependsOn(downloadIdeaDistributionZipAndExtractSelectedJars)

    val packages by extra {
        PackageListFromSimpleFile(file("referencedPackages.txt").path).pathsToInclude
    }
    val excludedPackages by extra {
        PackageListFromSimpleFile(file("notReferencedPackages.txt").path).pathsToInclude
    }
    val extractDir by extra { file("$downloadDir/dependencies") }

    doLast {
        val chosenJars: Set<String> by downloadIdeaDistributionZipAndExtractSelectedJars.get().extra
        for (library in chosenJars) {
            copy {
                from(zipTree("$libDir/$library.jar"))
                setIncludes(packages)
                setExcludes(excludedPackages)
                includeEmptyDirs = false
                into(extractDir)
            }
            file("$libDir/$library.jar").delete()
        }
    }
}

val downloadKotlinxLibraries by tasks.registering(Copy::class) {
    dependsOn(deleteLibrariesFromLibFolder)
    from(kotlinxLibraries)
    into(libDir)
    rename("(kotlinx-coroutines-\\w+)-.*", "$1.jar")
}

val createIdeDependenciesJar by tasks.registering(Jar::class) {
    dependsOn(extractSelectedFilesFromIdeaJars)
    dependsOn(downloadKotlinxLibraries)

    val extractDir: File by extractSelectedFilesFromIdeaJars.get().extra

    from(extractDir)
    destinationDirectory.set(libDir)
    archiveFileName.set("ide-dependencies.jar")

    manifest {
        attributes(mapOf("Built-By" to "JetBrains",
                "Implementation-Vendor" to "JetBrains",
                "Implementation-Version" to "1.0",
                "Implementation-Title" to "ide-dependencies"))
    }

    doLast {
        extractDir.deleteRecursively()
    }
}

val downloadIdeaAndKotlinCompilerSources by tasks.registering {
    val locallyDownloadedKotlinCompilerSourcesFile by extra { file("$downloadDir/kotlin-compiler-sources.jar") }
    val locallyDownloadedIdeaSourcesFile by extra { file("$downloadDir/idea-sdk-sources.jar") }

    doLast {
        if(!locallyDownloadedKotlinCompilerSourcesFile.exists()) {
            //TODO can we get the sources from somewhere?
            //tcArtifactsResolver.downloadTo(tcArtifactsResolver.KOTLIN_COMPILER_SOURCES_JAR, locallyDownloadedKotlinCompilerSourcesFile)
        }
        if(!locallyDownloadedIdeaSourcesFile.exists()) {
            ideaArtifactsResolver.downloadTo(ideaArtifactsResolver.IDEA_IC_SOURCES_JAR, locallyDownloadedIdeaSourcesFile)
        }
    }
}

val repackageIdeaAndKotlinCompilerSources by tasks.registering(Zip::class) {
    dependsOn(downloadIdeaAndKotlinCompilerSources)

    val locallyDownloadedKotlinCompilerSourcesFile: File by downloadIdeaAndKotlinCompilerSources.get().extra
    val locallyDownloadedIdeaSourcesFile: File by downloadIdeaAndKotlinCompilerSources.get().extra

    from(zipTree(locallyDownloadedKotlinCompilerSourcesFile))
    from(zipTree(locallyDownloadedIdeaSourcesFile))

    destinationDirectory.set(libDir)
    archiveFileName.set("kotlin-compiler-sources.jar")
}

val downloadBundled by tasks.registering {
    if (localTCArtifacts) {
        dependsOn(extractPackagesFromPlugin,
                extractPackagesFromKTCompiler,
                createIdeDependenciesJar,
                downloadKotlinxLibraries)
    } else {
        dependsOn(extractPackagesFromPlugin,
                extractPackagesFromKTCompiler,
                createIdeDependenciesJar,
                downloadKotlinxLibraries)
    }

    if (!ignoreSources) {
        dependsOn(repackageIdeaAndKotlinCompilerSources)
    }
}

val getBundled by tasks.registering {
    dependsOn(downloadTestData, downloadTestFrameworkDependencies, downloadBundled)
}
