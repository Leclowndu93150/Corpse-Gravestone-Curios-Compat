package com.leclowndu93150.accessoriescompat;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;

@Mod(Main.MODID)
public class Main {
    private static final int USELESS = 0;
    public static final String MODID = "accessoriescorpsecompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main() {
        Config.register();
        LOGGER.info("Hello from Accessories x Corpse/Gravestone Compat!");
    }
}
