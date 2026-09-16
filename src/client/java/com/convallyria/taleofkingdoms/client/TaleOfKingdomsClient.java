package com.convallyria.taleofkingdoms.client;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.client.entity.render.RenderSetup;
import com.convallyria.taleofkingdoms.client.gui.RenderListener;
import com.convallyria.taleofkingdoms.client.gui.generic.ScreenContinueConquest;
import com.convallyria.taleofkingdoms.client.gui.generic.ScreenStartConquest;
import com.convallyria.taleofkingdoms.client.gui.shop.ScreenSellItem;
import com.convallyria.taleofkingdoms.client.listener.ClientGameInstanceListener;
import com.convallyria.taleofkingdoms.client.listener.StartWorldListener;
import com.convallyria.taleofkingdoms.client.packet.both.BothSignContractPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.incoming.IncomingInstanceSyncPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.incoming.IncomingOpenScreenPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingBankerInteractPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingBuildKingdomPacket;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingBuyItemPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingCityBuilderActionPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingFixGuildPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingForemanBuyWorkerPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingForemanCollectPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingHunterPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingInnkeeperPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingToggleSellGuiPacketHandler;
import com.convallyria.taleofkingdoms.client.packet.outgoing.OutgoingUpgradeKingdomPacketHandler;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.packet.PacketHandler;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.server.packet.outgoing.OutgoingOpenScreenPacketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.io.File;

@Mod(value = TaleOfKingdoms.MODID, dist = Dist.CLIENT)
@Environment(EnvType.CLIENT)
public class TaleOfKingdomsClient implements ClientModInitializer {

    private static TaleOfKingdomsClientAPI api;
    private StartWorldListener startWorldListener;

    public static final KeyBinding START_CONQUEST_KEYBIND = new KeyBinding(
            "key.taleofkingdoms.startconquest", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.taleofkingdoms.keys" // The translation key of the keybinding's category.
    );

    public static TaleOfKingdomsClientAPI getAPI() {
        return api;
    }

    public TaleOfKingdomsClient(IEventBus modBus) {
        modBus.addListener(this::registerScreens);
        modBus.addListener(this::registerKeyMappings);
        modBus.addListener(this::clientSetup);
        NeoForge.EVENT_BUS.addListener(this::clientTick);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(this::onInitializeClient);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TaleOfKingdoms.SELL_SCREEN_HANDLER, ScreenSellItem::new);
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(START_CONQUEST_KEYBIND);
    }

    @Override
    public void onInitializeClient() {
        TaleOfKingdoms.setAPI(api = new TaleOfKingdomsClientAPI(TaleOfKingdoms.getInstance()));
        new RenderSetup(api.getMod());
        registerPacketHandlers();
        registerEvents();
        registerTasks();

    }

    private void clientTick(ClientTickEvent.Post event) {
        MinecraftClient client = MinecraftClient.getInstance();
        while (START_CONQUEST_KEYBIND.wasPressed()) {
            if (client.player == null || startWorldListener == null || api == null) return;

            String worldName = startWorldListener.getWorldName();
            if (worldName == null) {
                TaleOfKingdoms.LOGGER.info("World name was null");
                return;
            }

            var existingInstance = api.getConquestInstanceStorage().getConquestInstance(worldName);
            if (existingInstance.isPresent()) {
                client.setScreen(new ScreenContinueConquest(existingInstance.get()));
                return;
            }

            File file = new File(api.getDataFolder() + "worlds/" + worldName + ConquestInstance.FILE_TYPE);
            client.setScreen(new ScreenStartConquest(worldName, file, client.player));
        }
    }

    private void registerPacketHandlers() {
        registerHandler(new BothSignContractPacketHandler());

        registerHandler(new OutgoingBankerInteractPacketHandler());
        registerHandler(new OutgoingBuildKingdomPacket());
        registerHandler(new OutgoingBuyItemPacketHandler());
        registerHandler(new OutgoingCityBuilderActionPacketHandler());
        registerHandler(new OutgoingFixGuildPacketHandler());
        registerHandler(new OutgoingForemanBuyWorkerPacketHandler());
        registerHandler(new OutgoingForemanCollectPacketHandler());
        registerHandler(new OutgoingHunterPacketHandler());
        registerHandler(new OutgoingInnkeeperPacketHandler());
        registerHandler(new OutgoingToggleSellGuiPacketHandler());
        registerHandler(new OutgoingUpgradeKingdomPacketHandler());

        registerHandler(new IncomingInstanceSyncPacketHandler());
        registerHandler(new IncomingOpenScreenPacketHandler());

        api.registerPacketHandler(EnvType.SERVER, new OutgoingOpenScreenPacketHandler());
    }

    protected void registerHandler(PacketHandler<?> clientPacketHandler) {
        api.registerPacketHandler(EnvType.CLIENT, clientPacketHandler);
    }

    private void registerEvents() {
        TaleOfKingdoms.LOGGER.info("Registering client events...");
        this.startWorldListener = new StartWorldListener();
        new ClientGameInstanceListener();
        new RenderListener();
    }

    private void registerTasks() {
        api.getScheduler().repeating(server -> {
            api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                instance.getGuildPlayers().forEach((id, guildPlayer) -> {
                    final PlayerKingdom kingdom = guildPlayer.getKingdom();
                    if (kingdom == null) return;
                    kingdom.tryTaxCollection(guildPlayer);
                });
            });
        }, 20, 1000);
    }
}
