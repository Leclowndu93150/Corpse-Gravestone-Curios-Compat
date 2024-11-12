package com.leclowndu93150.corpsecurioscompat;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Main.MODID)
public class Main {

    public static final String MODID = "gravestonecurioscompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main() {
       LOGGER.info("Hello from Gravestone Curios Compat!");
    }

}
