package com.convallyria.taleofkingdoms.mixin.common;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Removes tracked attackers when they are destroyed without a normal death. */
@Mixin(Entity.class)
public abstract class EntityRemovalEvent {

    @Inject(method = "remove", at = @At("HEAD"))
    private void taleofkingdoms$removeTrackedAttacker(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity.getWorld().isClient() || !reason.shouldDestroy()) return;

        TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
        if (api == null) return;
        api.getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
            if (instance.getReficuleAttackers().remove(entity.getUuid())) {
                TaleOfKingdoms.LOGGER.info("Removed destroyed guild attacker {} ({})", entity.getUuid(), reason);
            }
        });
    }
}
