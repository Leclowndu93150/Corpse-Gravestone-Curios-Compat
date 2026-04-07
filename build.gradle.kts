plugins {
    id("dev.prism")
}

group = "com.leclowndu93150"
version = "4.0.0"

prism {
    curseMaven()

    mod("corpse-curios") {
        metadata {
            modId = "corpsecurioscompat"
            name = "Corpse Curios Compatibility"
            description = "Adds compatibility between Corpse and Curios"
            license = "MIT"
            author("Leclowndu93150")
            version = "4.0.1"
        }
        publishing {
            type = STABLE
            changelog = """
                
                 Fixes crash due to obfuscation issues, i'm sorry to all people who were affected
                
                - Respects Curios keepCurios config setting (fixes item deletion when keepCurios = ON)
                - 26.1 port
            """.trimIndent()

            curseforge {
                accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
                projectId = "1138130"
            }
            modrinth {
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")
                projectId = "pJGcKPh1"
            }

            dependencies {
                requires("corpse")
                requires("curios")
                requires("baguettelib")
            }
        }
        version("26.1") {
            minecraftVersions("26.1", "26.1.1")
            neoforge {
                loaderVersion = "26.1.0.1-beta"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:7806025")
                    implementation("curse.maven:curios-309927:7810501")
                    implementation("curse.maven:baguettelib-1264423:7812841")
                }
            }
        }
        version("1.21.1") {
            neoforge {
                loaderVersion = "21.1.171"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:7018307")
                    implementation("curse.maven:curios-309927:6529130")
                    implementation("curse.maven:baguettelib-1264423:7880467")
                    runtimeOnly("curse.maven:artifacts-312353:7875426")
                    runtimeOnly("curse.maven:cloth-config-348521:5729127")
                }
            }
        }
        version("1.20.1") {
            forge {
                loaderVersion = "47.4.0"
                loaderVersionRange = "[47,)"
                dependencies {
                    modImplementation("curse.maven:corpse-316582:7018272")
                    modImplementation("curse.maven:curios-309927:6418456")
                    modImplementation("curse.maven:baguettelib-1264423:7544807")
                    modRuntimeOnly("curse.maven:artifacts-312353:7880372")
                    modRuntimeOnly("curse.maven:cloth-config-348521:5729105")
                    modRuntimeOnly("curse.maven:architectury-api-419699:5137938")
                }
            }
        }
    }

    mod("gravestone-curios") {
        metadata {
            modId = "gravestonecurioscompat"
            name = "Gravestone Curios Compatibility"
            description = "Adds compatibility between Gravestone and Curios"
            license = "MIT"
            author("Leclowndu93150")
            version = "4.0.2"
        }
        publishing {
            type = STABLE
            changelog = """
                
                Fixes crash due to obfuscation issues, i'm sorry to all people who were affected
                
                Major rewrite of Gravestone Curios Compatibility:
                - Per-item slot tracking: curios now restore to their exact original slot
                - Cosmetic slot support: cosmetic curios are properly preserved and restored
                - Slot-adding item priority: items that add curio slots are restored first
                - Delayed restoration: prevents conflicts with slot initialization
                - Curse of Binding support: configurable handling of cursed items
                - Forced slot rebuild on restoration for accurate slot counts
                - Respects Curios keepCurios config setting (fixes item deletion when keepCurios = ON)
                - Player logout cleanup to prevent item loss
                - Item blacklist configuration
            """.trimIndent()

            curseforge {
                accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
                projectId = "1139062"
            }
            modrinth {
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")
                projectId = "NaiL8bPN"
            }

            dependencies {
                requires("gravestone-mod")
                requires("curios")
                requires("baguettelib")
            }
        }
        version("26.1") {
            minecraftVersions("26.1", "26.1.1")
            neoforge {
                loaderVersion = "26.1.0.1-beta"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:gravestone-mod-238551:7806149")
                    implementation("curse.maven:curios-309927:7810501")
                    implementation("curse.maven:baguettelib-1264423:7812841")
                }
            }
        }
        version("1.21.1") {
            neoforge {
                loaderVersion = "21.1.171"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:gravestone-mod-238551:7099728")
                    implementation("curse.maven:curios-309927:6529130")
                    implementation("curse.maven:baguettelib-1264423:7880467")
                    runtimeOnly("curse.maven:artifacts-312353:7875426")
                    runtimeOnly("curse.maven:cloth-config-348521:5729127")
                }
            }
        }
        version("1.20.1") {
            forge {
                loaderVersion = "47.4.0"
                loaderVersionRange = "[47,)"
                dependencies {
                    modImplementation("curse.maven:gravestone-mod-238551:7099725")
                    modImplementation("curse.maven:curios-309927:6418456")
                    modImplementation("curse.maven:baguettelib-1264423:7544807")
                    modRuntimeOnly("curse.maven:artifacts-312353:7880372")
                    modRuntimeOnly("curse.maven:cloth-config-348521:5729105")
                    modRuntimeOnly("curse.maven:architectury-api-419699:5137938")
                }
            }
        }
    }

    mod("accessories-compat") {
        metadata {
            modId = "accessoriescorpsecompat"
            name = "Corpse & Gravestone x Accessories Compat"
            description = "Adds compatibility between Corpse/Gravestone and the Accessories API"
            license = "MIT"
            author("Leclowndu93150")
            version = "1.0.0"
        }
        publishing {
            type = STABLE
            changelog = """
                Initial release
            """.trimIndent()

            curseforge {
                accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
                projectId = ""
            }
            modrinth {
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")
                projectId = ""
            }

            dependencies {
                requires("corpse")
                requires("accessories")
                requires("baguettelib")
            }
        }
        version("1.21.1") {
            neoforge {
                loaderVersion = "21.1.171"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:7018307")
                    implementation("curse.maven:gravestone-mod-238551:7099728")
                    implementation("curse.maven:accessories-938917:7583320")
                    implementation("curse.maven:baguettelib-1264423:7880467")
                    runtimeOnly("curse.maven:aether-255308:7043502")
                }
            }
        }
        version("1.20.1") {
            forge {
                loaderVersion = "47.4.0"
                loaderVersionRange = "[47,)"
                dependencies {
                    modImplementation("curse.maven:corpse-316582:7018272")
                    modImplementation("curse.maven:gravestone-mod-238551:7099725")
                    modImplementation("curse.maven:accessories-938917:7164863")
                    modImplementation("curse.maven:baguettelib-1264423:7544807")
                }
            }
        }
    }

    mod("cosmetic-corpse") {
        metadata {
            modId = "cosmeticcorpsecompat"
            name = "Cosmetic Armor x Corpse Compat"
            description = "Adds compatibility between Corpse and Cosmetic Armor Reworked"
            license = "MIT"
            author("Leclowndu93150")
            version = "4.0.1"
        }
        publishing {
            type = STABLE
            changelog = """
                
                 Fixes crash due to obfuscation issues, i'm sorry to all people who were affected
                 
                - Fixed dupe bug on 1.21.1 (missing isEditable check for history corpses)
            """.trimIndent()

            curseforge {
                accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
                projectId = "1208436"
            }
            modrinth {
                accessToken = providers.environmentVariable("MODRINTH_TOKEN")
                projectId = "VrbUxhCI"
            }

            dependencies {
                requires("corpse")
                requires("cosmetic-armor-reworked")
            }
        }
        version("1.21.1") {
            neoforge {
                loaderVersion = "21.1.171"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:7018307")
                    implementation("curse.maven:cosmetic-armor-reworked-237307:5610814")
                }
            }
        }
        version("1.20.1") {
            forge {
                loaderVersion = "47.4.0"
                loaderVersionRange = "[47,)"
                dependencies {
                    modImplementation("curse.maven:corpse-316582:7018272")
                    modImplementation("curse.maven:cosmetic-armor-reworked-237307:4600191")
                }
            }
        }
    }
}
