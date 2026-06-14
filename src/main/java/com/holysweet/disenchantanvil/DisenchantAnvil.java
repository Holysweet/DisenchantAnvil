package com.holysweet.disenchantanvil;

import com.holysweet.disenchantanvil.config.DAConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@Mod(DisenchantAnvil.MOD_ID)
public class DisenchantAnvil {
    public static final String MOD_ID = "disenchantanvil";

    public DisenchantAnvil(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, DAConfig.COMMON_SPEC);

        modBus.addListener(DisenchantAnvil::onConfigLoading);
        modBus.addListener(DisenchantAnvil::onConfigReloading);
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == DAConfig.COMMON_SPEC) {
            DAConfig.bakeCache();
        }
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == DAConfig.COMMON_SPEC) {
            DAConfig.bakeCache();
        }
    }
}