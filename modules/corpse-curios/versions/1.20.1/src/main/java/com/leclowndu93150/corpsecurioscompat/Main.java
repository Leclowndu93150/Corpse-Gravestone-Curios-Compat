package com.leclowndu93150.corpsecurioscompat;

import com.leclowndu93150.baguettelib.event.entity.death.PlayerDeathEvent;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Main.MODID)
public class Main {

    private static final int USELESS = 0;
    public static final String MODID = "corpsecurioscompat";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Main() {
        Config.register();
       LOGGER.info("Hello from Corpse Curios Compat!");
    }

}
