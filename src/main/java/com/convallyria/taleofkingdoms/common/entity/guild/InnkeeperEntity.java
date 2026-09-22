package com.convallyria.taleofkingdoms.common.entity.guild;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.entity.TOKEntity;
import com.convallyria.taleofkingdoms.common.entity.ai.goal.WanderAroundGuildGoal;
import com.convallyria.taleofkingdoms.common.packet.Packets;
import com.convallyria.taleofkingdoms.common.packet.s2c.OpenScreenPacket;
import com.convallyria.taleofkingdoms.common.translation.Translations;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class InnkeeperEntity extends TOKEntity {

    public InnkeeperEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 15.0F, 100F));
        this.goalSelector.add(2, new WanderAroundGuildGoal(this, 0.5, 25, 3, 1));
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND) return ActionResult.PASS;
        if (player.getWorld().isClient()) return ActionResult.SUCCESS;

        TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api == null || api.getConquestInstanceStorage().mostRecentInstance().isEmpty()) return ActionResult.FAIL;
        ConquestInstance instance = api.getConquestInstanceStorage().mostRecentInstance().get();
        final GuildPlayer guildPlayer = instance.getPlayer(player.getUuid());
        if (guildPlayer == null || !guildPlayer.hasSignedContract()) {
            Translations.NEED_CONTRACT.send(player);
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            api.getServerPacket(Packets.OPEN_CLIENT_SCREEN).sendPacket(serverPlayer, new OpenScreenPacket(OpenScreenPacket.ScreenTypes.INNKEEPER, this.getId()));
        }

        return ActionResult.SUCCESS;
    }

    // Disable jumping
    @Override
    public void jump() {}

    @Override
    public boolean isStationary() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
