plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.openapi.generator")
}

group = "com.salnikoff"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvm { withJava() }
    js(IR) {
        browser {}
    }
    linuxX64 { }
    macosArm64 {}

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("$buildDir/generate-resources/main/src/commonMain/kotlin")
            val serializationVersion: String by project
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }
    }
}

/**
 * Настраиваем генерацию здесь
 */
openApiGenerate {
    val openapiGroup = "${rootProject.group}.telegram"
    generatorName.set("kotlin") // Это и есть активный генератор
    packageName.set(openapiGroup)
    apiPackage.set("$openapiGroup.api")
    modelPackage.set("$openapiGroup.models")
    invokerPackage.set("$openapiGroup.invoker")
    inputSpec.set("$rootDir/specs/telegram-models-api-v1.yaml")
    library.set("multiplatform") // Используем библиотеку для KMP

    /**
     * Здесь указываем, что нам нужны только модели, все остальное не нужно
     */
    globalProperties.apply {
        put("models", "")
        put("modelDocs", "false")
    }

    /**
     * Настройка дополнительных параметров из документации по генератору
     * https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/kotlin.md
     */
    configOptions.set(
        mapOf(
            "dateLibrary" to "string",
            "enumPropertyNaming" to "UPPERCASE",
            "collectionType" to "list"
        )
    )
}

tasks {
    this.openApiGenerate {}
}

afterEvaluate {
    val openApiGenerate = tasks.getByName("openApiGenerate")
    tasks.filter { it.name.startsWith("compile") }.forEach {
        it.dependsOn(openApiGenerate)
    }
    tasks.filter { it.name.endsWith("Elements") }.forEach {
        it.dependsOn(openApiGenerate)
    }
}