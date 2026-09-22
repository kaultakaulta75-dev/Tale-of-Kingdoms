package com.convallyria.taleofkingdoms.common.listener;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.entity.generic.HunterEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildGuardEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WardenHireable;
import com.convallyria.taleofkingdoms.common.event.EntityDeathCallback;
import com.convallyria.taleofkingdoms.common.event.EntityPickupItemCallback;
import com.convallyria.taleofkingdoms.common.event.ItemMergeCallback;
import com.convallyria.taleofkingdoms.common.item.ItemHelper;
import com.convallyria.taleofkingdoms.common.item.ItemRegistry;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import com.convallyria.taleofkingdoms.common.world.guild.GuildQuestProgression;
import com.convallyria.taleofkingdoms.server.world.ServerConquestInstance;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.api.EnvType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CoinListener extends Listener {
    private JsonObject worthinessJson;

    public CoinListener() {
        loadWorthinessJson();
        dropCoinsOnDeath();
        coinPickup();
        preventCoinMerge();
    }

    /**
     * When an entity dies this event is called.
     * If the entity is a player then the player's coins are subtracted by the amount of their current coins divided by 20 and the method returns.
     * If the cause of death was a player then the {@link LivingEntity} drops their coins
     * and the player gets worthiness added equal to the {@link CoinListener#getMobWorthiness(Entity)} times (*) the {@link CoinListener#getDifficultyWorthinessMultiplier(World)}
     */
    private void dropCoinsOnDeath() {
        EntityDeathCallback.EVENT.register((source, entity) -> {
            TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                if (entity instanceof PlayerEntity) {
                    final GuildPlayer guildPlayer = instance.getPlayer(entity.getUuid());
                    if (guildPlayer == null) return;
                    int subtract = guildPlayer.getCoins() / 20;
                    if (subtract > 0) guildPlayer.trySpendCoins(subtract);
                    return;
                }

                if (entity instanceof HunterEntity) {
                    for (UUID playerUuid : instance.getGuildPlayers().keySet()) {
                        final GuildPlayer loopPlayer = instance.getPlayer(playerUuid);
                        final List<UUID> hunterUuids = loopPlayer.getHunters();
                        hunterUuids.remove(entity.getUuid());
                        TaleOfKingdoms.LOGGER.info("Hunter {} died and was removed", entity.getUuid());
                    }
                    return;
                }

                if (source.getSource() instanceof PlayerEntity
                        || source.getSource() instanceof HunterEntity
                        || source.getSource() instanceof WardenHireable
                        || source.getSource() instanceof GuildGuardEntity
                        || source.getSource() instanceof ProjectileEntity) {
                    if (source.getSource() instanceof ProjectileEntity projectileEntity
                            && !(projectileEntity.getOwner() instanceof PlayerEntity)
                            && !(projectileEntity.getOwner() instanceof HunterEntity)
                            && !(projectileEntity.getOwner() instanceof WardenHireable)) return;

                    ItemHelper.dropCoins(entity);

                    ServerPlayerEntity responsiblePlayer = getResponsiblePlayer(source.getSource());
                    if (responsiblePlayer != null) {
                        int reward = getMobWorthiness(entity) * getDifficultyWorthinessMultiplier(responsiblePlayer.getWorld());
                        GuildQuestProgression.rewardWorthiness(instance, responsiblePlayer, reward);
                        if (TaleOfKingdoms.getAPI().getEnvironment() == EnvType.SERVER)
                            ServerConquestInstance.sync(responsiblePlayer, instance);
                    }
                }
            });
        });
    }

    private ServerPlayerEntity getResponsiblePlayer(Entity sourceEntity) {
        if (sourceEntity instanceof ServerPlayerEntity serverPlayer) return serverPlayer;
        if (sourceEntity instanceof ProjectileEntity projectile) {
            if (projectile.getOwner() instanceof ServerPlayerEntity serverPlayer) return serverPlayer;
            if (projectile.getOwner() instanceof HunterEntity hunter) return getHunterOwner(hunter);
            if (projectile.getOwner() instanceof WardenHireable soldier) return getSoldierOwner(soldier);
        }
        if (sourceEntity instanceof HunterEntity hunter) return getHunterOwner(hunter);
        if (sourceEntity instanceof WardenHireable soldier) return getSoldierOwner(soldier);
        return null;
    }

    private ServerPlayerEntity getHunterOwner(HunterEntity hunter) {
        if (hunter.getOwnerUuid() == null || hunter.getServer() == null) return null;
        return hunter.getServer().getPlayerManager().getPlayer(hunter.getOwnerUuid());
    }

    private ServerPlayerEntity getSoldierOwner(WardenHireable soldier) {
        if (soldier.getOwnerUuid() == null || soldier.getServer() == null) return null;
        return soldier.getServer().getPlayerManager().getPlayer(soldier.getOwnerUuid());
    }

    private void coinPickup() {
        EntityPickupItemCallback.EVENT.register((player, item) -> {
            if (equalsCoin(item)) {
                TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                    Random random = ThreadLocalRandom.current();
                    instance.addCoins(player.getUuid(), random.nextInt(1, 6));
                    if (TaleOfKingdoms.getAPI().getEnvironment() == EnvType.SERVER) {
                        ServerConquestInstance.sync((ServerPlayerEntity) player, instance);
                    }
                });

                player.getInventory().remove(predicate -> predicate.getItem().equals(item.getItem()), -1, player.getInventory());
            }
        });
    }

    private void preventCoinMerge() {
        ItemMergeCallback.EVENT.register((stack1, stack2) -> !equalsCoin(stack1) || !equalsCoin(stack2));
    }

    private boolean equalsCoin(ItemStack stack) {
        return stack.getItem().equals(ItemRegistry.ITEMS.get(ItemRegistry.TOKItem.COIN));
    }

    //Copies (if needed) and loads the json file in "config/taleofkingdoms/worthiness.json"
    private void loadWorthinessJson() {
        File internalFile = new File("worthiness.json");
        File configDirectory = new File("config/" + TaleOfKingdoms.MODID);
        File externalFile = new File(configDirectory, internalFile.getName());

        try {
            Files.createDirectories(configDirectory.toPath());
            if (!externalFile.isFile()) {
                try (InputStream fileSrc = Thread.currentThread().getContextClassLoader().getResourceAsStream(internalFile.getPath())) {
                    if (fileSrc == null) throw new IOException("Missing bundled worthiness.json");
                    Files.copy(fileSrc, externalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            try (Reader reader = Files.newBufferedReader(externalFile.toPath())) {
                worthinessJson = new Gson().fromJson(reader, JsonObject.class);
            }
        } catch (IOException | JsonParseException error) {
            TaleOfKingdoms.LOGGER.error("Unable to load worthiness configuration", error);
            worthinessJson = new JsonObject();
        }
    }

    /**
     * Gets the mob worthiness from the json file
     * @param mob the {@link LivingEntity} killed
     * @return the mob's worthiness if and only if the entry exists, else 1
     */
    public int getMobWorthiness(Entity mob) {
        String mobType = mob.getType().getName().getString();

        if(worthinessJson != null && worthinessJson.has(mobType)) {
            return worthinessJson.get(mobType).getAsInt();
        } else {
            return 1;
        }
    }

    /**
     * Gets the difficulty worthiness from the json file
     * @param world the {@link World} the entity died in
     * @return the difficulty's worthiness if and only if the entry exists, else 1
     */
    public int getDifficultyWorthinessMultiplier(World world) {
        if (worthinessJson == null || !worthinessJson.has("difficulty")) return 1;
        JsonObject difficulty = worthinessJson.getAsJsonObject("difficulty");

        if(difficulty.has(world.getDifficulty().getName())) {
            return difficulty.get(world.getDifficulty().getName()).getAsInt();
        } else {
            return 1;
        }
    }
}
