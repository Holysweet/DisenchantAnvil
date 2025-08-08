package com.holysweet.disenchantanvil.logic;

import com.holysweet.disenchantanvil.DisenchantAnvil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles anvil landing processing:
 * - find item entities exactly on the impact block,
 * - identify one enchanted input + normal books,
 * - extract enchants in stored order (top→down), 1 book per enchant (curses allowed),
 * - spawn single-enchant enchanted books,
 * - consume the same number of normal books,
 * - gently push everything outward so the anvil can occupy the space.
 */
public class AnvilProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger("DisenchantAnvil/Processor");

    public static void processLanding(LevelAccessor world, BlockPos anvilPos) {
        if (!(world instanceof Level level) || level.isClientSide) return;

        // Axis-aligned box covering ONLY the impact block
        AABB box = AABB.unitCubeFromLowerCorner(anvilPos.getCenter().subtract(0.5, 0.5, 0.5));
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);
        if (items.isEmpty()) return;

        // Find one enchanted input + all normal books
        ItemEntity inputEntity = null;
        ItemStack inputStack = ItemStack.EMPTY;
        boolean inputIsBook = false;

        int totalBooks = 0;
        List<ItemEntity> bookEntities = new ArrayList<>();

        for (ItemEntity ent : items) {
            ItemStack st = ent.getItem();
            if (st.isEmpty()) continue;

            if (st.is(Items.BOOK)) {
                totalBooks += st.getCount();
                bookEntities.add(ent);
                continue;
            }

            if (inputEntity == null && isEnchanted(st)) {
                inputEntity = ent;
                inputStack = st;
                inputIsBook = st.is(Items.ENCHANTED_BOOK);
            }
        }

        if (inputEntity == null || totalBooks <= 0) {
            // Still push out so the anvil can sit there
            pushOutItems(items, anvilPos);
            return;
        }

        // Read enchants from the correct component
        ItemEnchantments existing = inputIsBook
                ? inputStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY)
                : inputStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (existing.isEmpty()) {
            pushOutItems(items, anvilPos);
            return;
        }

        // Make a mutable copy for removals
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(existing);

        // Build ordered lists (top→down as provided by the component)
        List<Holder<Enchantment>> enchList = new ArrayList<>();
        List<Integer> lvlList = new ArrayList<>();
        for (Holder<Enchantment> ench : existing.keySet()) {
            enchList.add(ench);
            lvlList.add(existing.getLevel(ench));
        }

        int moves = Math.min(enchList.size(), totalBooks);
        if (moves == 0) {
            pushOutItems(items, anvilPos);
            return;
        }

        // Produce single-enchant enchanted books
        List<ItemStack> producedBooks = new ArrayList<>(moves);
        for (int i = 0; i < moves; i++) {
            Holder<Enchantment> ench = enchList.get(i);
            int lvl = lvlList.get(i);

            // Remove from input by setting level 0
            mutable.set(ench, 0);

            // Create an enchanted book with exactly this one enchant
            ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantments.Mutable single = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            single.set(ench, lvl);
            newBook.set(DataComponents.STORED_ENCHANTMENTS, single.toImmutable());
            producedBooks.add(newBook);
        }

        // Write back remaining enchants to input
        if (inputIsBook) {
            inputStack.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        } else {
            inputStack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }

        // Consume N normal books across the stacks present
        int toConsume = moves;
        for (ItemEntity be : bookEntities) {
            if (toConsume <= 0) break;
            ItemStack bs = be.getItem();
            int take = Math.min(bs.getCount(), toConsume);
            bs.shrink(take);
            toConsume -= take;
            if (bs.isEmpty()) be.discard();
        }

        // Spawn produced enchanted books at the impact position (they’ll be pushed too)
        for (ItemStack pb : producedBooks) {
            ItemEntity out = new ItemEntity(level,
                    anvilPos.getX() + 0.5,
                    anvilPos.getY() + 0.5,
                    anvilPos.getZ() + 0.5,
                    pb);
            level.addFreshEntity(out);
            items.add(out);
        }

        // Finally, push everything outward so the anvil occupies the block
        pushOutItems(items, anvilPos);

        LOGGER.info("[{}] Extracted {} enchantment(s) into books; remaining on item: {}",
                DisenchantAnvil.MOD_ID, producedBooks.size(),
                (inputIsBook
                        ? inputStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).size()
                        : inputStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).size()));
    }

    private static boolean isEnchanted(ItemStack st) {
        if (st.is(Items.ENCHANTED_BOOK)) {
            return !st.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
        }
        return !st.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    private static void pushOutItems(List<ItemEntity> items, BlockPos anvilPos) {
        final double nudge = 0.08;
        final double lift = 0.02;
        int i = 0;
        for (ItemEntity it : items) {
            if (!it.isAlive()) continue;
            int dir = (i++) & 3; // +X, -X, +Z, -Z cycle
            double dx = (dir == 0) ? nudge : (dir == 1) ? -nudge : 0.0;
            double dz = (dir == 2) ? nudge : (dir == 3) ? -nudge : 0.0;
            double px = anvilPos.getX() + 0.5 + dx * 2.0;
            double py = anvilPos.getY() + 0.2;
            double pz = anvilPos.getZ() + 0.5 + dz * 2.0;
            it.setPos(px, py, pz);
            it.setDeltaMovement(dx, lift, dz);
            it.setOnGround(false);
            it.hasImpulse = true;
        }
    }
}
