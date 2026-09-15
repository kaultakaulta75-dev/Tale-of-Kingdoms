package com.convallyria.taleofkingdoms.common.entity;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
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
import com.convallyria.taleofkingdoms.common.entity.kingdom.StockMarketEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.ArcherHireableEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WardenEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.warden.WarriorHireableEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.LumberForemanEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.LumberWorkerEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.QuarryForemanEntity;
import com.convallyria.taleofkingdoms.common.entity.kingdom.workers.QuarryWorkerEntity;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleGuardianEntity;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleMageEntity;
import com.convallyria.taleofkingdoms.common.entity.reficule.ReficuleSoldierEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.util.Identifier;

import java.util.List;

public class EntityTypes {

    private static final float HUMAN_WIDTH = 0.6f;
    private static final float HUMAN_HEIGHT = 1.8f;

    public static EntityType<FarmerEntity> FARMER;
    public static EntityType<GuildMasterEntity> GUILDMASTER;
    public static EntityType<GuildMasterDefenderEntity> GUILDMASTER_DEFENDER;
    public static EntityType<BlacksmithEntity> BLACKSMITH;
    public static EntityType<CityBuilderEntity> CITYBUILDER;
    public static EntityType<KnightEntity> KNIGHT;
    public static EntityType<InnkeeperEntity> INNKEEPER;
    public static EntityType<HunterEntity> HUNTER;
    public static EntityType<GuildGuardEntity> GUILDGUARD;
    public static EntityType<GuildVillagerEntity> GUILDVILLAGER;
    public static EntityType<GuildArcherEntity> GUILDARCHER;
    public static EntityType<LoneEntity> LONE;
    public static EntityType<BankerEntity> BANKER;
    public static EntityType<FoodShopEntity> FOODSHOP;
    public static EntityType<GuildCaptainEntity> GUILDCAPTAIN;
    public static EntityType<LoneVillagerEntity> LONEVILLAGER;

    public static EntityType<ReficuleSoldierEntity> REFICULE_SOLDIER;
    public static EntityType<ReficuleGuardianEntity> REFICULE_GUARDIAN;
    public static EntityType<ReficuleMageEntity> REFICULE_MAGE;

    public static EntityType<BanditEntity> BANDIT;

    // =========================
    // Player's kingdom entities
    // =========================
    public static EntityType<ItemShopEntity> ITEM_SHOP;

    public static EntityType<KingdomVillagerEntity> KINGDOM_VILLAGER;

    public static EntityType<StockMarketEntity> STOCK_MARKET;

    public static EntityType<QuarryForemanEntity> QUARRY_FOREMAN;

    public static EntityType<LumberForemanEntity> LUMBER_FOREMAN;

    public static EntityType<QuarryWorkerEntity> QUARRY_WORKER;

    public static EntityType<LumberWorkerEntity> LUMBER_WORKER;

    public static EntityType<WardenEntity> WARDEN;

    public static EntityType<WarriorHireableEntity> WARRIOR;

    public static EntityType<ArcherHireableEntity> ARCHER;

    public static EntityType<BlockShopEntity> BLOCK_SHOP;

    public static EntityType<HumanFarmerEntity> HUMAN_FARMER;

    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE.getKey(), helper -> {
            FARMER = EntityType.Builder.create(FarmerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:farmer")
            GUILDMASTER = EntityType.Builder.create(GuildMasterEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_master")
            GUILDMASTER_DEFENDER = EntityType.Builder.create(GuildMasterDefenderEntity::new, SpawnGroup.MISC).makeFireImmune().dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_master_defender")
            BLACKSMITH = EntityType.Builder.create(BlacksmithEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:blacksmith")
            CITYBUILDER = EntityType.Builder.create(CityBuilderEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:city_builder")
            KNIGHT = EntityType.Builder.create(KnightEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:knight")
            INNKEEPER = EntityType.Builder.create(InnkeeperEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:innkeeper")
            HUNTER = EntityType.Builder.create(HunterEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:hunter")
            GUILDGUARD = EntityType.Builder.create(GuildGuardEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_guard")
            GUILDVILLAGER = EntityType.Builder.create(GuildVillagerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_villager")
            GUILDARCHER = EntityType.Builder.create(GuildArcherEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_archer")
            LONE = EntityType.Builder.create(LoneEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lone")
            BANKER = EntityType.Builder.create(BankerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:banker")
            FOODSHOP = EntityType.Builder.create(FoodShopEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:foodshop")
            GUILDCAPTAIN = EntityType.Builder.create(GuildCaptainEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_captain")
            LONEVILLAGER = EntityType.Builder.create(LoneVillagerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lone_villager")
            REFICULE_SOLDIER = EntityType.Builder.create(ReficuleSoldierEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:reficule_soldier")
            REFICULE_GUARDIAN = EntityType.Builder.create(ReficuleGuardianEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:reficule_guardian")
            REFICULE_MAGE = EntityType.Builder.create(ReficuleMageEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:reficule_mage")
            BANDIT = EntityType.Builder.create(BanditEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:bandit")
            ITEM_SHOP = EntityType.Builder.create(ItemShopEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:item_shop")
            KINGDOM_VILLAGER = EntityType.Builder.create(KingdomVillagerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:kingdom_villager")
            STOCK_MARKET = EntityType.Builder.create(StockMarketEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:stock_market")
            QUARRY_FOREMAN = EntityType.Builder.create(QuarryForemanEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:quarry_foreman")
            LUMBER_FOREMAN = EntityType.Builder.create(LumberForemanEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lumber_foreman")
            QUARRY_WORKER = EntityType.Builder.create(QuarryWorkerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:quarry_worker")
            LUMBER_WORKER = EntityType.Builder.create(LumberWorkerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lumber_worker")
            WARDEN = EntityType.Builder.create(WardenEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:warden")
            WARRIOR = EntityType.Builder.create(WarriorHireableEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:warrior_mercenary")
            ARCHER = EntityType.Builder.create(ArcherHireableEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:archer_mercenary")
            BLOCK_SHOP = EntityType.Builder.create(BlockShopEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:block_shop")
            HUMAN_FARMER = EntityType.Builder.create(HumanFarmerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:human_farmer")

            helper.register(Identifier.of(TaleOfKingdoms.MODID, "farmer"), FARMER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "guild_master"), GUILDMASTER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "guild_master_defender"), GUILDMASTER_DEFENDER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "blacksmith"), BLACKSMITH);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "city_builder"), CITYBUILDER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "knight"), KNIGHT);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "innkeeper"), INNKEEPER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "hunter"), HUNTER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "guild_guard"), GUILDGUARD);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "guild_villager"), GUILDVILLAGER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "guild_archer"), GUILDARCHER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "lone"), LONE);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "banker"), BANKER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "foodshop"), FOODSHOP);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "guild_captain"), GUILDCAPTAIN);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "lone_villager"), LONEVILLAGER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "reficule_soldier"), REFICULE_SOLDIER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "reficule_guardian"), REFICULE_GUARDIAN);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "reficule_mage"), REFICULE_MAGE);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "bandit"), BANDIT);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "item_shop"), ITEM_SHOP);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "kingdom_villager"), KINGDOM_VILLAGER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "stock_market"), STOCK_MARKET);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "quarry_foreman"), QUARRY_FOREMAN);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "lumber_foreman"), LUMBER_FOREMAN);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "quarry_worker"), QUARRY_WORKER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "lumber_worker"), LUMBER_WORKER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "warden"), WARDEN);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "warrior_mercenary"), WARRIOR);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "archer_mercenary"), ARCHER);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "block_shop"), BLOCK_SHOP);
            helper.register(Identifier.of(TaleOfKingdoms.MODID, "human_farmer"), HUMAN_FARMER);
            SHOP_ENTITIES = List.of(BLACKSMITH, ITEM_SHOP, FOODSHOP, BLOCK_SHOP, STOCK_MARKET);
        });
    }

    public static List<EntityType<? extends ShopEntity>> SHOP_ENTITIES;
}
