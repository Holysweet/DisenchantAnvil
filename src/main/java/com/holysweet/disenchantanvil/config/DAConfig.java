package com.holysweet.disenchantanvil.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DAConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BASE_BLOCKS;

    private static final Set<Block> DEFAULT_BASE_BLOCKS = Set.of(
            Blocks.OBSIDIAN,
            Blocks.CRYING_OBSIDIAN
    );

    // volatile ensures thread-safe reading mid-game
    private static volatile Set<Block> cachedBlocks = DEFAULT_BASE_BLOCKS;

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
                        DAConfig::isValidBlockId
                );

        builder.pop();
        COMMON_SPEC = builder.build();
    }

    /**
     * Call this method from ModConfigEvent.Loading/Reloading event listeners
     * to safely bake the config strings into actual Block objects.
     */
    public static void bakeCache() {
        Set<Block> resolved = ALLOWED_BASE_BLOCKS.get().stream()
                .map(Identifier::tryParse)
                .filter(java.util.Objects::nonNull)
                .flatMap(id -> BuiltInRegistries.BLOCK.get(id).stream())
                .map(ref -> ref.value())
                .collect(Collectors.toSet());

        cachedBlocks = resolved.isEmpty() ? DEFAULT_BASE_BLOCKS : Set.copyOf(resolved);
    }

    public static boolean isAllowedBase(Block block) {
        // Zero logic processing here. Just a lightning-fast O(1) hash set look-up during gameplay.
        return cachedBlocks.contains(block);
    }

    private static boolean isValidBlockId(Object value) {
        if (!(value instanceof String id)) {
            return false;
        }

        return Identifier.tryParse(id) != null;
    }

    private DAConfig() {}
}