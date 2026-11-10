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
            version = "4.2.1"
        }
        publishing {
            type = STABLE
            changelog = """
                - Fixed the item blacklist not working on 1.21.1 and newer
                - Item blacklist now accepts item tags (e.g. #modid:tag)
                - Added blacklisted_slots config: curio slot types that items are never restored into
                - Added swap_occupied_slots config: if the original slot is taken, the restored item goes back in and the current item moves to the inventory (Curse of Binding items are never swapped out)
                - Fixed curios being lost or put in the wrong slot (cosmetic vs normal) when restored alongside slot-adding items
                - Fixed curios that could not be re-equipped keeping leftover slot data
                - Removed debug log spam when checking for cursed items
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
        version("26.3") {
            neoforge {
                loaderVersion = "26.3.0.102"
                dependencies {
                    implementation("curse.maven:corpse-316582:9289341")
                    implementation("curse.maven:curios-309927:9521516")
                    implementation("curse.maven:baguettelib-1264423:9134480")
                }
            }
        }
        version("26.2") {
            neoforge {
                loaderVersion = "26.2.0.93"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:9289335")
                    implementation("curse.maven:curios-309927:8478313")
                    implementation("curse.maven:baguettelib-1264423:8887468")
                }
            }
        }
        version("26.1") {
            minecraftVersions("26.1", "26.1.1", "26.1.2")
            neoforge {
                loaderVersion = "26.1.0.1-beta"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:9289330")
                    implementation("curse.maven:curios-309927:8463519")
                    implementation("curse.maven:baguettelib-1264423:7813553")
                }
            }
        }
        version("1.21.1") {
            neoforge {
                loaderVersion = "21.1.171"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:corpse-316582:9289316")
                    implementation("curse.maven:curios-309927:6529130")
                    implementation("curse.maven:baguettelib-1264423:8887463")
                    runtimeOnly("curse.maven:artifacts-312353:8791899")
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
                    modImplementation("curse.maven:baguettelib-1264423:8887462")
                    modRuntimeOnly("curse.maven:artifacts-312353:7903806")
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
            version = "4.2.1"
        }
        publishing {
            type = STABLE
            changelog = """
                - Fixed the item blacklist not working on 1.21.1 and newer
                - Item blacklist now accepts item tags (e.g. #modid:tag)
                - Added blacklisted_slots config: curio slot types that items are never restored into
                - Added swap_occupied_slots config: if the original slot is taken, the restored item goes back in and the current item moves to the inventory (Curse of Binding items are never swapped out)
                - Fixed curios being lost or put in the wrong slot (cosmetic vs normal) when restored alongside slot-adding items
                - Fixed curios that could not be re-equipped keeping leftover slot data
                - Fixed curios in slots added by another curio (via item NBT) not returning to their slot on 1.20.1
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
        version("26.3") {
            neoforge {
                loaderVersion = "26.3.0.102"
                dependencies {
                    implementation("curse.maven:gravestone-mod-238551:9027231")
                    implementation("curse.maven:curios-309927:9521516")
                    implementation("curse.maven:baguettelib-1264423:9134480")
                }
            }
        }
        version("26.2") {
            neoforge {
                loaderVersion = "26.2.0.93"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:gravestone-mod-238551:8671632")
                    implementation("curse.maven:curios-309927:8478313")
                    implementation("curse.maven:baguettelib-1264423:8887468")
                }
            }
        }
        version("26.1") {
            minecraftVersions("26.1", "26.1.1", "26.1.2")
            neoforge {
                loaderVersion = "26.1.0.1-beta"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:gravestone-mod-238551:8671629")
                    implementation("curse.maven:curios-309927:8463519")
                    implementation("curse.maven:baguettelib-1264423:7813553")
                }
            }
        }
        version("1.21.1") {
            neoforge {
                loaderVersion = "21.1.171"
                loaderVersionRange = "[4,)"
                dependencies {
                    implementation("curse.maven:gravestone-mod-238551:8671622")
                    implementation("curse.maven:curios-309927:6529130")
                    implementation("curse.maven:baguettelib-1264423:8887463")
                    runtimeOnly("curse.maven:artifacts-312353:8791899")
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
                    modImplementation("curse.maven:baguettelib-1264423:8887462")
                    modRuntimeOnly("curse.maven:artifacts-312353:7903806")
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
                    implementation("curse.maven:corpse-316582:9289316")
                    implementation("curse.maven:gravestone-mod-238551:8671622")
                    implementation("curse.maven:accessories-938917:9216447")
                    implementation("curse.maven:baguettelib-1264423:8887463")
                    runtimeOnly("curse.maven:aether-255308:7043502")
                }
            }
        }
        version("1.20.1") {
            changelog = "- Fixed missing refmap entry in mixin config"
            forge {
                loaderVersion = "47.4.0"
                loaderVersionRange = "[47,)"
                dependencies {
                    modImplementation("curse.maven:corpse-316582:7018272")
                    modImplementation("curse.maven:gravestone-mod-238551:7099725")
                    modImplementation("curse.maven:accessories-938917:7164863")
                    modImplementation("curse.maven:baguettelib-1264423:8887462")
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
                    implementation("curse.maven:corpse-316582:9289316")
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
