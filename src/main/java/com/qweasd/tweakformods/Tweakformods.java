package com.qweasd.tweakformods;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Tweakformods.MODID)
public final class Tweakformods {
    public static final String MODID = "tweakformods";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Tweakformods(ModContainer modContainer) {
        modContainer.getEventBus().addListener(Config::onConfigChanged);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
