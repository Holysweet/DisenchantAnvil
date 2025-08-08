package com.holysweet.disenchantanvil.event;

import com.holysweet.disenchantanvil.DisenchantAnvil;
import com.holysweet.disenchantanvil.logic.AnvilProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

@EventBusSubscriber(modid = DisenchantAnvil.MOD_ID)
public final class FallingAnvilHandler {

    private FallingAnvilHandler() {}

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof FallingBlockEntity fbe)) return;
        if (!(fbe.getBlockState().getBlock() instanceof AnvilBlock)) return;

        // Need a real Level and server side
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;

        // Where it *thinks* it landed
        BlockPos landPos = fbe.blockPosition();

        // Safety: chunk might be unloading during shutdown
        if (!level.isLoaded(landPos)) return;

        // After leave, the block at landPos should now be placed; verify and check the floor
        if (!(level.getBlockState(landPos).getBlock() instanceof AnvilBlock)) return;

        var floor = level.getBlockState(landPos.below()).getBlock();
        if (floor != Blocks.OBSIDIAN && floor != Blocks.CRYING_OBSIDIAN) return;

        AnvilProcessor.processLanding(level, landPos);
    }
}
