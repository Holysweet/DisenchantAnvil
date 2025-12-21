package com.holysweet.disenchantanvil.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DAConfig {

    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BASE_BLOCKS;

    private static Set<Block> cachedBlocks;

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
                        () -> "minecraft:obsidian", // ✅ new element supplier (this is the non-deprecated overload)
                        o -> o instanceof String    // element validator
                );

        builder.pop();
        COMMON_SPEC = builder.build();
    }

    public static boolean isAllowedBase(Block block) {
        if (cachedBlocks == null) {
            cachedBlocks = ALLOWED_BASE_BLOCKS.get().stream()
                    .map(ResourceLocation::tryParse)
                    .map(BuiltInRegistries.BLOCK::get)
                    .collect(Collectors.toSet());
        }
        return cachedBlocks.contains(block);
    }

    private DAConfig() {}
}
