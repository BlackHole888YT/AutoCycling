package org.blackhole;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AutoCycling.MODID)
public class AutoCycling {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "autocycling";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec.IntValue AUTO_TRADER_SPEED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        AUTO_TRADER_SPEED = builder
                .comment("Delay in ticks between auto trade rerolls")
                .defineInRange("AutoTraderSpeed", 10, 1, 200);
        CLIENT_CONFIG = builder.build();
    }


    public AutoCycling() {
        FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG, "auto_cycling.toml");
    }

    public static int getAutoTraderSpeed() {
        return Math.max(1, AUTO_TRADER_SPEED.get());
    }

}
