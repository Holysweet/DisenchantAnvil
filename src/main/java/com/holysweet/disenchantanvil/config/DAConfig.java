package com.holysweet.disenchantanvil.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DAConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BASE_BLOCKS;

    // volatile ensures thread-safe reading mid-game
    private static volatile Set<Block> cachedBlocks = Collections.emptySet();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("disenchanting");

        ALLOWED_BASE_BLOCKS = builder
                .comment("Blocks an anvil may land on to trigger disenchanting")
                .defineListAllowEmpty(
                        "allowedBaseBlocks",
                        List.of(
                                "minecraft:obsidian",
                                "minecraft:crying_obsidian"
                        ),
                        () -> "minecraft:obsidian",
                        o -> o instanceof String
                );

        builder.pop();
        COMMON_SPEC = builder.build();
    }

    /**
     * Call this method from your main mod constructor or ModConfigEvent.Loading/Reloading event listeners
     * to safely bake the config strings into actual Block objects during startup.
     */
    public static void bakeCache() {
        cachedBlocks = ALLOWED_BASE_BLOCKS.get().stream()
                .map(ResourceLocation::tryParse) // Safely filter out any garbage entries that failed to parse
                .filter(java.util.Objects::nonNull)
                .map(BuiltInRegistries.BLOCK::get)
                .collect(Collectors.toSet());
    }

    public static boolean isAllowedBase(Block block) {
        // Zero logic processing here. Just a lightning-fast O(1) hash set look-up during gameplay.
        return cachedBlocks.contains(block);
    }

    private DAConfig() {}
}