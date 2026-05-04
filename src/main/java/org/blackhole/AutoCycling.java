package org.blackhole;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AutoCycling.MODID)
public class AutoCycling {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "autocycling";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    public AutoCycling() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();


    }

}
