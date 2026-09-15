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

    public static final EntityType<FarmerEntity> FARMER =
            EntityType.Builder.create(FarmerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:farmer");
    public static final EntityType<GuildMasterEntity> GUILDMASTER =
            EntityType.Builder.create(GuildMasterEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_master");
    public static final EntityType<GuildMasterDefenderEntity> GUILDMASTER_DEFENDER =
            EntityType.Builder.create(GuildMasterDefenderEntity::new, SpawnGroup.MISC).makeFireImmune().dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_master_defender");
    public static final EntityType<BlacksmithEntity> BLACKSMITH =
            EntityType.Builder.create(BlacksmithEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:blacksmith");
    public static final EntityType<CityBuilderEntity> CITYBUILDER =
            EntityType.Builder.create(CityBuilderEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:city_builder");
    public static final EntityType<KnightEntity> KNIGHT =
            EntityType.Builder.create(KnightEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:knight");
    public static final EntityType<InnkeeperEntity> INNKEEPER =
            EntityType.Builder.create(InnkeeperEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:innkeeper");
    public static final EntityType<HunterEntity> HUNTER =
            EntityType.Builder.create(HunterEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:hunter");
    public static final EntityType<GuildGuardEntity> GUILDGUARD =
            EntityType.Builder.create(GuildGuardEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_guard");
    public static final EntityType<GuildVillagerEntity> GUILDVILLAGER =
            EntityType.Builder.create(GuildVillagerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_villager");
    public static final EntityType<GuildArcherEntity> GUILDARCHER =
            EntityType.Builder.create(GuildArcherEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_archer");
    public static final EntityType<LoneEntity> LONE =
            EntityType.Builder.create(LoneEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lone");
    public static final EntityType<BankerEntity> BANKER =
            EntityType.Builder.create(BankerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:banker");
    public static final EntityType<FoodShopEntity> FOODSHOP =
            EntityType.Builder.create(FoodShopEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:foodshop");
    public static final EntityType<GuildCaptainEntity> GUILDCAPTAIN =
            EntityType.Builder.create(GuildCaptainEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:guild_captain");
    public static final EntityType<LoneVillagerEntity> LONEVILLAGER =
            EntityType.Builder.create(LoneVillagerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lone_villager");

    public static final EntityType<ReficuleSoldierEntity> REFICULE_SOLDIER =
            EntityType.Builder.create(ReficuleSoldierEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:reficule_soldier");
    public static final EntityType<ReficuleGuardianEntity> REFICULE_GUARDIAN =
            EntityType.Builder.create(ReficuleGuardianEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:reficule_guardian");
    public static final EntityType<ReficuleMageEntity> REFICULE_MAGE =
            EntityType.Builder.create(ReficuleMageEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:reficule_mage");

    public static final EntityType<BanditEntity> BANDIT =
            EntityType.Builder.create(BanditEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:bandit");

    // =========================
    // Player's kingdom entities
    // =========================
    public static final EntityType<ItemShopEntity> ITEM_SHOP =
            EntityType.Builder.create(ItemShopEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:item_shop");

    public static final EntityType<KingdomVillagerEntity> KINGDOM_VILLAGER =
            EntityType.Builder.create(KingdomVillagerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:kingdom_villager");

    public static final EntityType<StockMarketEntity> STOCK_MARKET =
            EntityType.Builder.create(StockMarketEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:stock_market");

    public static final EntityType<QuarryForemanEntity> QUARRY_FOREMAN =
            EntityType.Builder.create(QuarryForemanEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:quarry_foreman");

    public static final EntityType<LumberForemanEntity> LUMBER_FOREMAN =
            EntityType.Builder.create(LumberForemanEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lumber_foreman");

    public static final EntityType<QuarryWorkerEntity> QUARRY_WORKER =
            EntityType.Builder.create(QuarryWorkerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:quarry_worker");

    public static final EntityType<LumberWorkerEntity> LUMBER_WORKER =
            EntityType.Builder.create(LumberWorkerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:lumber_worker");

    public static final EntityType<WardenEntity> WARDEN =
            EntityType.Builder.create(WardenEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:warden");

    public static final EntityType<WarriorHireableEntity> WARRIOR =
            EntityType.Builder.create(WarriorHireableEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:warrior_mercenary");

    public static final EntityType<ArcherHireableEntity> ARCHER =
            EntityType.Builder.create(ArcherHireableEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:archer_mercenary");

    public static final EntityType<BlockShopEntity> BLOCK_SHOP =
            EntityType.Builder.create(BlockShopEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:block_shop");

    public static final EntityType<HumanFarmerEntity> HUMAN_FARMER =
            EntityType.Builder.create(HumanFarmerEntity::new, SpawnGroup.MISC).dimensions(HUMAN_WIDTH, HUMAN_HEIGHT).build("taleofkingdoms:human_farmer");

    public static void register(RegisterEvent event) {
        event.register(Registries.ENTITY_TYPE.getKey(), helper -> {
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
        });
    }

    public static final List<EntityType<? extends ShopEntity>> SHOP_ENTITIES = List.of(EntityTypes.BLACKSMITH, EntityTypes.ITEM_SHOP, EntityTypes.FOODSHOP, EntityTypes.BLOCK_SHOP, EntityTypes.STOCK_MARKET);
}
