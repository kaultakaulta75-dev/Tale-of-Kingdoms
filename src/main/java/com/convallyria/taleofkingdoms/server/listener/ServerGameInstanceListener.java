package com.convallyria.taleofkingdoms.server.listener;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.event.GameInstanceCallback;
import com.convallyria.taleofkingdoms.common.event.PlayerJoinCallback;
import com.convallyria.taleofkingdoms.common.event.PlayerJoinWorldCallback;
import com.convallyria.taleofkingdoms.common.event.PlayerLeaveCallback;
import com.convallyria.taleofkingdoms.common.event.tok.KingdomStartCallback;
import com.convallyria.taleofkingdoms.common.generator.processor.GuildStructureProcessor;
import com.convallyria.taleofkingdoms.common.listener.Listener;
import com.convallyria.taleofkingdoms.common.schematic.Schematic;
import com.convallyria.taleofkingdoms.common.schematic.SchematicOptions;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.server.TaleOfKingdomsServer;
import com.convallyria.taleofkingdoms.server.TaleOfKingdomsServerAPI;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import com.google.gson.Gson;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.SERVER)
public class ServerGameInstanceListener extends Listener {

    public ServerGameInstanceListener() {
        final TaleOfKingdomsServerAPI api = TaleOfKingdomsServer.getAPI();
        GameInstanceCallback.EVENT.register(api::setServer);

        PlayerJoinCallback.EVENT.register((no, player) -> api.executeOnDedicatedServer(() -> {
            MinecraftDedicatedServer server = api.getServer();
            if (api.getConquestInstanceStorage().getConquestInstance(server.getLevelName()).isPresent()) return;

            File conquestFile = new File(api.getDataFolder() + "worlds/" + server.getLevelName() + ConquestInstance.FILE_TYPE);
            Gson gson = api.getMod().getGson();
            Optional<ConquestInstance> savedInstance = ConquestInstance.load(conquestFile, gson);
            if (savedInstance.isPresent() && savedInstance.get().isLoaded()) {
                ConquestInstance instance = savedInstance.get();
                api.getConquestInstanceStorage().addConquest(server.getLevelName(), instance, true);
                if (!instance.areGuildFieldsRestored()) {
                    try {
                        GuildStructureProcessor.restoreExistingFarms(server.getOverworld(), instance.getOrigin().down(21));
                        instance.setGuildFieldsRestored(true);
                        instance.save(server.getLevelName());
                    } catch (Exception error) {
                        TaleOfKingdoms.LOGGER.error("Unable to restore guild fields for {}", server.getLevelName(), error);
                    }
                }
            } else {
                this.create(api, player, server);
            }
        }));

        PlayerJoinWorldCallback.EVENT.register(player -> {
            api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                if (TaleOfKingdoms.getAPI().getEnvironment() == EnvType.SERVER) {
                    if (!instance.hasPlayer(player.getUuid())) {
                        instance.reset(player);
                    }

                    ServerConquestInstance.sync(player, instance);
                }
            });
        });

        PlayerLeaveCallback.EVENT.register(player -> api.executeOnDedicatedServer(() -> {
            final MinecraftDedicatedServer server = api.getServer();
            api.getConquestInstanceStorage().getConquestInstance(server.getLevelName()).ifPresent(conquestInstance -> {
                conquestInstance.save(server.getLevelName());
            });
        }));
    }

    private CompletableFuture<Void> create(TaleOfKingdomsAPI api, ServerPlayerEntity player, MinecraftDedicatedServer server) {
        // int topY = server.getOverworld().getTopY(Heightmap.Type.MOTION_BLOCKING, 0, 0);
        BlockPos pastePos = player.getBlockPos().subtract(new Vec3i(0, 20, 0));
        ConquestInstance instance = new ConquestInstance(server.getName(), null, null, player.getBlockPos().add(0, 1, 0));
        instance.reset(player);
        api.getConquestInstanceStorage().addConquest(server.getLevelName(), instance, true);
        return api.getSchematicHandler().pasteSchematic(Schematic.GUILD_CASTLE, player, pastePos, SchematicOptions.ALIGN_TO_TERRAIN).thenAccept(oi -> {
            BlockPos start = new BlockPos(oi.getMaxX(), oi.getMaxY(), oi.getMaxZ());
            BlockPos end = new BlockPos(oi.getMinX(), oi.getMinY(), oi.getMinZ());
            instance.setStart(start);
            instance.setEnd(end);
            instance.setOrigin(new BlockPos(oi.getMinX(), oi.getMinY() + 21, oi.getMinZ()));
            
            TaleOfKingdoms.LOGGER.info("Summoning citizens of the realm...");
            KingdomStartCallback.EVENT.invoker().kingdomStart(player, instance); // Call kingdom start event
            instance.setLoaded(true);
            /*instance.reset(player);
            instance.sync(player);*/
            instance.save(server.getLevelName());
        }).exceptionally(error -> {
            api.getConquestInstanceStorage().removeConquest(server.getLevelName());
            TaleOfKingdoms.LOGGER.error("Dedicated-server castle generation failed", error);
            return null;
        });
    }
}
