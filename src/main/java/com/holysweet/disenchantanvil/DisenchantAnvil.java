package com.holysweet.disenchantanvil;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import com.holysweet.disenchantanvil.config.DAConfig;
import net.neoforged.fml.config.ModConfig;

@Mod(DisenchantAnvil.MOD_ID)
public class DisenchantAnvil {
    public static final String MOD_ID = "disenchantanvil";

    public DisenchantAnvil(IEventBus modBus, ModContainer container) {
        // Register the common config (generates config/disenchantanvil-common.toml)
        container.registerConfig(ModConfig.Type.COMMON, DAConfig.COMMON_SPEC);
        // Nothing to do here for event handlers using @EventBusSubscriber.
        // (Register DeferredRegisters here later if you add them.)
    }
}
