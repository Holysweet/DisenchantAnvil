package com.holysweet.disenchantanvil.event;

import com.holysweet.disenchantanvil.DisenchantAnvil;
import com.holysweet.disenchantanvil.config.DAConfig;
import com.holysweet.disenchantanvil.logic.AnvilProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
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

        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;

        BlockPos landPos = fbe.blockPosition();
        if (!level.isLoaded(landPos)) return;

        if (!(level.getBlockState(landPos).getBlock() instanceof AnvilBlock)) return;

        var floorBlock = level.getBlockState(landPos.below()).getBlock();
        if (!DAConfig.isAllowedBase(floorBlock)) return;

        AnvilProcessor.processLanding(level, landPos);
    }
}
