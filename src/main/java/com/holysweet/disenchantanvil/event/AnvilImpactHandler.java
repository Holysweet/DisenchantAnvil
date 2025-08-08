package com.holysweet.disenchantanvil.event;

import com.holysweet.disenchantanvil.DisenchantAnvil;
import com.holysweet.disenchantanvil.logic.AnvilProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for when a FALLING ANVIL becomes a placed block.
 * We use EntityPlaceEvent because FallingBlock placement is attributed to an 'entity'.
 */
public class AnvilImpactHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("DisenchantAnvil/Impact");

    @SubscribeEvent
    public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        // Placed state (the new block that ended up in the world)
        Block placed = event.getPlacedBlock().getBlock();
        if (!(placed instanceof AnvilBlock)) return;

        BlockPos pos = event.getPos();
        LevelAccessor level = event.getLevel();

        // Must land on obsidian / crying obsidian
        Block floor = level.getBlockState(pos.below()).getBlock();
        boolean isValidFloor = (floor == Blocks.OBSIDIAN) || (floor == Blocks.CRYING_OBSIDIAN);
        if (!isValidFloor) return;

        LOGGER.info("[{}] Falling anvil landed at {} on {} — processing items.",
                DisenchantAnvil.MOD_ID,
                pos,
                (floor == Blocks.CRYING_OBSIDIAN ? "crying_obsidian" : "obsidian"));

        // Hand off to the processor:
        // - find item entities exactly on the impact block
        // - identify enchanted input + normal books
        // - extract enchants in stored (NBT) order, 1 book per enchant
        // - curses produce cursed books
        // - push remaining stacks outward so the anvil occupies the space
        try {
            AnvilProcessor.processLanding(level, pos);
        } catch (Exception ex) {
            // Fail-safe: never break world placement due to our logic
            LOGGER.error("[{}] Error during anvil processing at {}: {}", DisenchantAnvil.MOD_ID, pos, ex.toString());
        }
    }
}
