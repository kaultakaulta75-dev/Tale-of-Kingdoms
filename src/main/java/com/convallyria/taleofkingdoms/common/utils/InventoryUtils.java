package com.convallyria.taleofkingdoms.common.utils;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class InventoryUtils {

    @Nullable
    public static ItemStack getStack(PlayerInventory playerInventory, TagKey<Item> tag, int count) {
        for (ItemStack itemStack : playerInventory.main) {
            if (itemStack.getItem().getRegistryEntry().isIn(tag)) { //todo for 1.18.2: check if this works
                if (itemStack.getCount() == count) {
                    return itemStack;
                }
            }
        }
        return null;
    }

    public static int getSlotWithStack(PlayerInventory playerInventory, ItemStack stack) {
        for (int i = 0; i < playerInventory.main.size(); ++i) {
            if (!playerInventory.main.get(i).isEmpty() && areItemsEqual(stack, playerInventory.main.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static int getSlotWithStack(SimpleInventory inventory, ItemStack stack) {
        for (int i = 0; i < inventory.size(); ++i) {
            if (!inventory.getStack(i).isEmpty() && areItemsEqual(stack, inventory.getStack(i))) {
                return i;
            }
        }
        return -1;
    }

    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && ItemStack.areItemsAndComponentsEqual(stack1, stack2);
    }

    public static int count(Inventory inventory, TagKey<Item> tag) {
        int i = 0;
        for (int j = 0; j < inventory.size(); ++j) {
            ItemStack itemStack = inventory.getStack(j);
            if (!itemStack.getItem().getRegistryEntry().isIn(tag)) continue;
            i += itemStack.getCount();
        }
        return i;
    }

    public static boolean remove(Inventory inventory, TagKey<Item> tag, int count) {
        return remove(inventory, stack -> stack.getItem().getRegistryEntry().isIn(tag), count);
    }

    public static boolean remove(Inventory inventory, Item item, int count) {
        return remove(inventory, stack -> stack.isOf(item), count);
    }

    private static boolean remove(Inventory inventory, Predicate<ItemStack> predicate, int count) {
        if (count <= 0) return true;

        int available = 0;
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (predicate.test(stack)) available += stack.getCount();
        }
        if (available < count) return false;

        int remaining = count;
        for (int slot = 0; slot < inventory.size() && remaining > 0; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (!predicate.test(stack)) continue;
            int removed = Math.min(remaining, stack.getCount());
            stack.decrement(removed);
            remaining -= removed;
        }
        inventory.markDirty();
        return true;
    }
}
