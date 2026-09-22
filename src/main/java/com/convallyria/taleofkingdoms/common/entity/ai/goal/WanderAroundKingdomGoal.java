package com.convallyria.taleofkingdoms.common.entity.ai.goal;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import com.convallyria.taleofkingdoms.common.world.guild.GuildPlayer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class WanderAroundKingdomGoal extends WanderAroundGuildGoal {

    public WanderAroundKingdomGoal(PathAwareEntity mob, double speed) { this(mob, speed, 50); }

    public WanderAroundKingdomGoal(PathAwareEntity pathAwareEntity, double speed, int chance) {
        this(pathAwareEntity, speed, chance, 30, 7);
    }

    public WanderAroundKingdomGoal(PathAwareEntity pathAwareEntity, double speed, int chance, int horizontalRange, int verticalRange) {
        super(pathAwareEntity, speed, chance, horizontalRange, verticalRange);
    }

    @Override
    public boolean canStart() {
        final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api == null) return false;
        Optional<ConquestInstance> optionalInstance = api.getConquestInstanceStorage().mostRecentInstance();
        if (optionalInstance.isEmpty()) return false;
        ConquestInstance instance = optionalInstance.get();

        PlayerKingdom home = null;
        double closestDistance = Double.MAX_VALUE;
        for (GuildPlayer guildPlayer : instance.getGuildPlayers().values()) {
            PlayerKingdom kingdom = guildPlayer.getKingdom();
            if (kingdom == null || kingdom.getOrigin() == null) continue;
            if (kingdom.isInKingdom(this.mob.getBlockPos())) {
                home = kingdom;
                break;
            }
            BlockPos origin = kingdom.getOrigin();
            BlockPos mobPos = this.mob.getBlockPos();
            double deltaX = origin.getX() - mobPos.getX();
            double deltaY = origin.getY() - mobPos.getY();
            double deltaZ = origin.getZ() - mobPos.getZ();
            double distance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            if (distance < closestDistance) {
                closestDistance = distance;
                home = kingdom;
            }
        }
        if (home == null) return false;

        if (!home.isInKingdom(this.mob.getBlockPos())) {
            BlockPos origin = home.getOrigin();
            this.targetX = origin.getX() + 0.5;
            this.targetY = origin.getY();
            this.targetZ = origin.getZ() + 0.5;
            this.ignoringChance = false;
            return true;
        }

       if (!this.ignoringChance) {
           // if (this.field_24463 && this.mob.getDespawnCounter() >= 100) {
             //   return false;
          //  }

           if (this.mob.getRandom().nextInt(this.chance) != 0) {
               return false;
           }
       }

        Vec3d vec3d = this.getWanderTarget();
        if (vec3d == null) {
            return false;
        } else {
            BlockPos blockPos = BlockPos.ofFloored(vec3d.x, vec3d.y, vec3d.z);
            if (!home.isInKingdom(blockPos)) return false;

            this.targetX = vec3d.x;
            this.targetY = vec3d.y;
            this.targetZ = vec3d.z;
            this.ignoringChance = false;
            return true;
        }
    }
}
