package com.convallyria.taleofkingdoms.common.shop;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import net.fabricmc.api.EnvType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class SellScreenHandler extends ScreenHandler {

    private final Inventory inventory;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public SellScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1));
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public SellScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(TaleOfKingdoms.SELL_SCREEN_HANDLER, syncId);
        checkSize(inventory, 1);
        this.inventory = inventory;
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player);

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        int m;
        int l;
        // Our inventory
        this.addSlot(new Slot(inventory, 0, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });
        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    public static NamedScreenHandlerFactory createFactory() {
        return new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, player) -> new SellScreenHandler(syncId, playerInventory, new SimpleInventory(1)),
                Text.translatable("menu.taleofkingdoms.shop.sell")
        );
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    /**
     * @param clickData 0 = left click, 1 = right, 2 = middle, 3-x = extra mouse buttons
     */
    @Override
    public void onSlotClick(int slotIndex, int clickData, SlotActionType actionType, PlayerEntity playerEntity) {
        if (slotIndex == 0 && actionType == SlotActionType.PICKUP && (clickData == 0 || clickData == 1)) {
            ItemStack itemStack = playerEntity.currentScreenHandler.getCursorStack();
            if (itemStack.isEmpty()) return;

            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            if (api == null) return;
            Optional<ConquestInstance> instance = api.getConquestInstanceStorage().mostRecentInstance();
            if (instance.isEmpty()) return;

            for (List<ShopItem> shopItems : ShopParser.SHOP_ITEMS.values()) {
                for (ShopItem shopItem : shopItems) {
                    if (itemStack.getItem() != shopItem.getItem()) continue;
                    if (shopItem.getSell() <= 0) continue;

                    int itemStackCount = clickData == 0 ? itemStack.getCount() : 1;
                    long proceeds = (long) shopItem.getSell() * itemStackCount;
                    if (proceeds <= 0 || proceeds > Integer.MAX_VALUE) return;

                    // The handler runs on both logical sides. Only the authoritative server may
                    // mutate the wallet; this also prevents double payment in an integrated server.
                    if (!playerEntity.getWorld().isClient()) {
                        GuildPlayer guildPlayer = instance.get().getGuildPlayers().get(playerEntity.getUuid());
                        if (guildPlayer == null || !guildPlayer.tryCreditCoins((int) proceeds)) return;
                        if (playerEntity instanceof ServerPlayerEntity serverPlayer && api.getEnvironment() == EnvType.SERVER) {
                            ServerConquestInstance.sync(serverPlayer, instance.get());
                        }
                    }

                    if (clickData == 0) {
                        // Only set empty once we've found the item... and that the item is a valid sell item and that they left-clicked on the slot
                        playerEntity.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                    } else {
                        itemStack.decrement(1);
                        playerEntity.currentScreenHandler.setCursorStack(itemStack);
                    }
                    return;
                }
            }
            return;
        }
        if (slotIndex == 0) return;
        super.onSlotClick(slotIndex, clickData, actionType, playerEntity);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        return ItemStack.EMPTY;
    }
}
