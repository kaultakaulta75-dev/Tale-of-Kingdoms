package com.convallyria.taleofkingdoms;

import com.convallyria.taleofkingdoms.common.block.SellBlock;
import com.convallyria.taleofkingdoms.common.block.entity.SellBlockEntity;
import com.convallyria.taleofkingdoms.common.config.TaleOfKingdomsConfig;
import com.convallyria.taleofkingdoms.common.entity.EntityTypes;
import com.convallyria.taleofkingdoms.common.entity.generic.BanditEntity;
import com.convallyria.taleofkingdoms.common.entity.generic.HunterEntity;
import com.convallyria.taleofkingdoms.common.entity.generic.KnightEntity;
import com.convallyria.taleofkingdoms.common.entity.generic.LoneVillagerEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.BankerEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.BlacksmithEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.CityBuilderEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.FarmerEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.FoodShopEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildArcherEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildCaptainEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildGuardEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildMasterDefenderEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildMasterEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.GuildVillagerEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.InnkeeperEntity;
import com.convallyria.taleofkingdoms.common.entity.guild.LoneEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.BlockShopEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.HumanFarmerEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.ItemShopEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.KingdomVillagerEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.ArcherHireableEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WardenEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WarriorHireableEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.LumberForemanEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.LumberWorkerEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.QuarryForemanEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.QuarryWorkerEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.StockMarketEntity;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleGuardianEntity;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleMageEntity;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleSoldierEntity;
import com.convallyria.taleofkingdoms.common.generator.processor.GatewayStructureProcessor;
import com.convallyria.taleofkingdoms.common.generator.processor.GuildStructureProcessor;
import com.convallyria.taleofkingdoms.common.generator.processor.PlayerKingdomStructureProcessor;
import com.convallyria.taleofkingdoms.common.generator.structure.TOKStructures;
import com.convallyria.taleofkingdoms.common.item.ItemRegistry;
import com.convallyria.taleofkingdoms.common.listener.BlockListener;
import com.convallyria.taleofkingdoms.common.listener.CoinListener;
import com.convallyria.taleofkingdoms.common.listener.DeleteWorldListener;
import com.convallyria.taleofkingdoms.common.listener.KingdomListener;
import com.convallyria.taleofkingdoms.common.listener.MobDeathListener;
import com.convallyria.taleofkingdoms.common.listener.MobSpawnListener;
import com.convallyria.taleofkingdoms.common.listener.SleepListener;
import com.convallyria.taleofkingdoms.common.serialization.gson.ConquestInstanceAdapter;
import com.convallyria.taleofkingdoms.common.shop.SellScreenHandler;
import com.convallyria.taleofkingdoms.common.shop.ShopParser;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.managers.SoundManager;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Mod(TaleOfKingdoms.MODID)
public class TaleOfKingdoms implements ModInitializer {

    public static final String MODID = "taleofkingdoms";

    public static int DATA_FORMAT_VERSION = 1;

    public static final Logger LOGGER = LogManager.getLogger();

    // Woo! Static!
    private static TaleOfKingdoms instance;
    private static TaleOfKingdomsAPI api;
    public static TaleOfKingdomsConfig CONFIG;

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    public static final StructureProcessorType<GuildStructureProcessor> GUILD_PROCESSOR = () -> GuildStructureProcessor.CODEC;
    public static final StructureProcessorType<GatewayStructureProcessor> GATEWAY_PROCESSOR = () -> GatewayStructureProcessor.CODEC;
    public static final StructureProcessorType<PlayerKingdomStructureProcessor> KINGDOM_PROCESSOR = () -> PlayerKingdomStructureProcessor.CODEC;

    // A public identifier for multiple parts of our bigger chest
    public static final Identifier SELL_BLOCK_IDENTIFIER = Identifier.of(MODID, "sell_block");
    public static final ScreenHandlerType<SellScreenHandler> SELL_SCREEN_HANDLER =
            new ScreenHandlerType<>(SellScreenHandler::new, FeatureFlags.VANILLA_FEATURES);
    public static Block SELL_BLOCK;
    public static BlockEntityType<SellBlockEntity> SELL_BLOCK_ENTITY;

    public static void setAPI(TaleOfKingdomsAPI api) {
        if (TaleOfKingdoms.api != null) {
            throw new IllegalArgumentException("API already set!");
        }
        TaleOfKingdoms.api = api;
    }

    public static TaleOfKingdoms getInstance() {
        return instance;
    }

    public TaleOfKingdoms(IEventBus modBus) {
        modBus.addListener(this::registerContent);
        modBus.addListener(this::registerEntityAttributes);
        onInitialize();
    }

    @Override
    public void onInitialize() {
        instance = this;
        File file = new File(this.getDataFolder() + "worlds");
        if (!file.exists()) file.mkdirs();

        File legacy = new File(this.getLegacyDataFolder() + "worlds");
        if (legacy.exists()) {
            LOGGER.warn("Copying legacy worlds folder to new folder...");
            boolean success = true;
            for (File oldWorld : legacy.listFiles()) {
                if (oldWorld.getName().endsWith(".conquestworld")) {
                    LOGGER.warn("Copying {}...", oldWorld.getName());
                    try {
                        Files.move(oldWorld, new File(file + File.separator + oldWorld.getName().replace(".conquestworld", ConquestInstance.FILE_TYPE)));
                    } catch (IOException e) {
                        LOGGER.error("Error copying old world {}", oldWorld.getName(), e);
                        success = false;
                    }
                }
            }

            if (success) {
                legacy.delete();
                new File(this.getLegacyDataFolder()).delete();
            }
        }

        registerEvents();
        registerCommands();




        // Player's kingdom entities

        // Load shop items
        new ShopParser().createShopItems();
        ShopParser.SHOP_ITEMS.values().forEach(shopItems -> shopItems.forEach(shopItem -> LOGGER.info("Loaded item value {}", shopItem.toString())));
        CONFIG = AutoConfig.register(TaleOfKingdomsConfig.class, PartitioningSerializer.wrap(Toml4jConfigSerializer::new)).getConfig();
    }

    /**
     * Gets the "data folder" of the mod. This is always the modid as a folder in the mods folder.
     * You may get the file using this.
     * @return data folder name
     */
    @NotNull
    public String getDataFolder() {
        return new File(".").getAbsolutePath() + File.separator + "config" + File.separator + TaleOfKingdoms.MODID + File.separator;
    }

    @Deprecated
    public String getLegacyDataFolder() {
        return new File(".").getAbsolutePath() + File.separator + "mods" + File.separator + TaleOfKingdoms.MODID + File.separator;
    }

    /**
     * Gets the API. This will only be present after the mod has finished loading.
     * @return api of {@link TaleOfKingdoms}
     */
    public static TaleOfKingdomsAPI getAPI() {
        return api;
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityTypes.INNKEEPER, InnkeeperEntity.createMobAttributes().build());
        event.put(EntityTypes.FARMER, FarmerEntity.createMobAttributes().build());
        event.put(EntityTypes.GUILDMASTER, GuildMasterEntity.createMobAttributes().build());
        event.put(EntityTypes.GUILDMASTER_DEFENDER, GuildMasterDefenderEntity.createMobAttributes().build());
        event.put(EntityTypes.BLACKSMITH, BlacksmithEntity.createMobAttributes().build());
        event.put(EntityTypes.CITYBUILDER, CityBuilderEntity.createMobAttributes().build());
        event.put(EntityTypes.KNIGHT, KnightEntity.createMobAttributes().build());
        event.put(EntityTypes.HUNTER, HunterEntity.createMobAttributes().build());
        event.put(EntityTypes.GUILDGUARD, GuildGuardEntity.createMobAttributes().build());
        event.put(EntityTypes.GUILDVILLAGER, GuildVillagerEntity.createMobAttributes().build());
        event.put(EntityTypes.GUILDARCHER, GuildArcherEntity.createMobAttributes().build());
        event.put(EntityTypes.BANKER, BankerEntity.createMobAttributes().build());
        event.put(EntityTypes.LONE, LoneEntity.createMobAttributes().build());
        event.put(EntityTypes.FOODSHOP, FoodShopEntity.createMobAttributes().build());
        event.put(EntityTypes.GUILDCAPTAIN, GuildCaptainEntity.createMobAttributes().build());
        event.put(EntityTypes.LONEVILLAGER, LoneVillagerEntity.createMobAttributes().build());
        event.put(EntityTypes.REFICULE_SOLDIER, ReficuleSoldierEntity.createMobAttributes().build());
        event.put(EntityTypes.REFICULE_GUARDIAN, ReficuleGuardianEntity.createMobAttributes().build());
        event.put(EntityTypes.REFICULE_MAGE, ReficuleMageEntity.createMobAttributes().build());
        event.put(EntityTypes.BANDIT, BanditEntity.createMobAttributes().build());
        event.put(EntityTypes.ITEM_SHOP, ItemShopEntity.createMobAttributes().build());
        event.put(EntityTypes.KINGDOM_VILLAGER, KingdomVillagerEntity.createMobAttributes().build());
        event.put(EntityTypes.STOCK_MARKET, StockMarketEntity.createMobAttributes().build());
        event.put(EntityTypes.QUARRY_FOREMAN, QuarryForemanEntity.createMobAttributes().build());
        event.put(EntityTypes.LUMBER_FOREMAN, LumberForemanEntity.createMobAttributes().build());
        event.put(EntityTypes.QUARRY_WORKER, QuarryWorkerEntity.createMobAttributes().build());
        event.put(EntityTypes.LUMBER_WORKER, LumberWorkerEntity.createMobAttributes().build());
        event.put(EntityTypes.WARDEN, WardenEntity.createMobAttributes().build());
        event.put(EntityTypes.WARRIOR, WarriorHireableEntity.createMobAttributes().build());
        event.put(EntityTypes.ARCHER, ArcherHireableEntity.createMobAttributes().build());
        event.put(EntityTypes.BLOCK_SHOP, BlockShopEntity.createMobAttributes().build());
        event.put(EntityTypes.HUMAN_FARMER, HumanFarmerEntity.createVillagerAttributes().build());
    }

    private void registerEvents() {
        TaleOfKingdoms.LOGGER.info("Registering events...");
        new CoinListener();
        new SleepListener();
        new MobSpawnListener();
        new MobDeathListener();
        new BlockListener();
        new KingdomListener();
        new DeleteWorldListener();
    }

    private void registerCommands() {
        new TaleOfKingdomsCommands();
    }

    private void registerContent(RegisterEvent event) {
        event.register(Registries.STRUCTURE_PROCESSOR.getKey(), helper -> {
            helper.register(Identifier.of(MODID, "guild"), GUILD_PROCESSOR);
            helper.register(Identifier.of(MODID, "gateway"), GATEWAY_PROCESSOR);
            helper.register(Identifier.of(MODID, "kingdom"), KINGDOM_PROCESSOR);
        });
        event.register(Registries.SCREEN_HANDLER.getKey(),
                Identifier.of(MODID, "sell_screen_handler"), () -> SELL_SCREEN_HANDLER);
        event.register(Registries.BLOCK.getKey(), helper -> {
            SELL_BLOCK = new SellBlock(FabricBlockSettings.copyOf(Blocks.CHEST));
            helper.register(SELL_BLOCK_IDENTIFIER, SELL_BLOCK);
        });
        event.register(Registries.BLOCK_ENTITY_TYPE.getKey(), helper -> {
            SELL_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(SellBlockEntity::new, SELL_BLOCK).build(null);
            helper.register(SELL_BLOCK_IDENTIFIER, SELL_BLOCK_ENTITY);
        });
        event.register(Registries.STRUCTURE_PIECE.getKey(), helper -> {
            helper.register(Identifier.of(MODID, "bandit_camp_piece"), TOKStructures.BANDIT_CAMP);
            helper.register(Identifier.of(MODID, "gateway_piece"), TOKStructures.GATEWAY);
            helper.register(Identifier.of(MODID, "reficule_village_piece"), TOKStructures.REFICULE_VILLAGE);
        });
        event.register(Registries.STRUCTURE_TYPE.getKey(), helper -> {
            helper.register(Identifier.of(MODID, "bandit_camp"), TOKStructures.BANDIT_CAMP_TYPE);
            helper.register(Identifier.of(MODID, "gateway"), TOKStructures.GATEWAY_TYPE);
            helper.register(Identifier.of(MODID, "reficule_village"), TOKStructures.REFICULE_VILLAGE_TYPE);
        });
        EntityTypes.register(event);
        ItemRegistry.register(event);
        SoundManager.register(event);
    }

    public static Text parse(StringReader stringReader, RegistryWrapper.WrapperLookup registries) throws CommandSyntaxException {
        try {
            Text text = Text.Serialization.fromJson(stringReader.getString(), registries);
            if (text == null) {
                throw TextArgumentType.INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, "empty");
            } else {
                return text;
            }
        } catch (JsonParseException var4) {
            String string = var4.getCause() != null ? var4.getCause().getMessage() : var4.getMessage();
            throw TextArgumentType.INVALID_COMPONENT_EXCEPTION.createWithContext(stringReader, string);
        }
    }

    public Gson getGson() {
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(ConquestInstance.class, new ConquestInstanceAdapter())
                .create();
    }
}