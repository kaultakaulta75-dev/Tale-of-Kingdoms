package com.convallyria.taleofkingdoms.common.world.guild;

import com.convallyria.taleofkingdoms.common.kingdom.KingdomTier;
import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class GuildQuestProgression {

    public static final int DEFEND_GUILD_WORTHINESS = 750;
    public static final int FOUND_KINGDOM_WORTHINESS = 1500;
    public static final int TIER_TWO_WORTHINESS = 3000;

    private GuildQuestProgression() {
    }

    public static boolean canFoundKingdom(ConquestInstance instance, GuildPlayer guildPlayer) {
        return guildPlayer != null
                && guildPlayer.hasSignedContract()
                && guildPlayer.hasRebuiltGuild()
                && !instance.isUnderAttack()
                && guildPlayer.getKingdom() == null
                && guildPlayer.getWorthiness() >= FOUND_KINGDOM_WORTHINESS;
    }

    public static boolean canUpgradeTo(ConquestInstance instance, GuildPlayer guildPlayer, KingdomTier nextTier) {
        return guildPlayer != null
                && guildPlayer.hasSignedContract()
                && guildPlayer.hasRebuiltGuild()
                && !instance.isUnderAttack()
                && guildPlayer.getKingdom() != null
                && guildPlayer.getWorthiness() >= nextTier.getRequiredWorthiness();
    }

    public static void announceWorthinessMilestones(ServerPlayerEntity player, int previous, int current) {
        if (previous < DEFEND_GUILD_WORTHINESS && current >= DEFEND_GUILD_WORTHINESS) {
            player.sendMessage(Text.translatable("message.taleofkingdoms.quest.guild_defense_unlocked"), false);
        }
        if (previous < FOUND_KINGDOM_WORTHINESS && current >= FOUND_KINGDOM_WORTHINESS) {
            player.sendMessage(Text.translatable("message.taleofkingdoms.quest.kingdom_unlocked"), false);
        }
        if (previous < TIER_TWO_WORTHINESS && current >= TIER_TWO_WORTHINESS) {
            player.sendMessage(Text.translatable("message.taleofkingdoms.quest.tier_two_unlocked"), false);
        }
    }

    public static int rewardWorthiness(ConquestInstance instance, ServerPlayerEntity player, int amount) {
        GuildPlayer guildPlayer = instance.getPlayer(player);
        if (guildPlayer == null) return 0;
        int previous = guildPlayer.getWorthiness();
        int gained = guildPlayer.addWorthiness(amount);
        if (gained <= 0) return 0;
        announceWorthinessMilestones(player, previous, guildPlayer.getWorthiness());
        instance.attack(player, player.getServerWorld());
        return gained;
    }

    public static int getCurrentTarget(ConquestInstance instance, GuildPlayer guildPlayer) {
        if (!guildPlayer.hasSignedContract() || guildPlayer.getWorthiness() < DEFEND_GUILD_WORTHINESS
                || instance.isUnderAttack() || !guildPlayer.hasRebuiltGuild()) {
            return DEFEND_GUILD_WORTHINESS;
        }
        PlayerKingdom kingdom = guildPlayer.getKingdom();
        if (kingdom == null) return FOUND_KINGDOM_WORTHINESS;
        return kingdom.getTier().next().map(KingdomTier::getRequiredWorthiness)
                .orElse(Math.max(TIER_TWO_WORTHINESS, guildPlayer.getWorthiness()));
    }

    public static Text getCurrentObjective(ConquestInstance instance, GuildPlayer guildPlayer) {
        if (!guildPlayer.hasSignedContract()) {
            return Text.translatable("message.taleofkingdoms.quest.objective.sign_contract");
        }
        if (guildPlayer.getWorthiness() < DEFEND_GUILD_WORTHINESS) {
            return Text.translatable("message.taleofkingdoms.quest.objective.worthiness",
                    guildPlayer.getWorthiness(), DEFEND_GUILD_WORTHINESS);
        }
        if (instance.isUnderAttack()) {
            return Text.translatable("message.taleofkingdoms.quest.objective.defend_guild");
        }
        if (!guildPlayer.hasRebuiltGuild()) {
            return Text.translatable("message.taleofkingdoms.quest.objective.rebuild_guild");
        }
        if (guildPlayer.getKingdom() == null) {
            if (guildPlayer.getWorthiness() < FOUND_KINGDOM_WORTHINESS) {
                return Text.translatable("message.taleofkingdoms.quest.objective.worthiness",
                        guildPlayer.getWorthiness(), FOUND_KINGDOM_WORTHINESS);
            }
            return Text.translatable("message.taleofkingdoms.quest.objective.found_kingdom");
        }

        PlayerKingdom kingdom = guildPlayer.getKingdom();
        return kingdom.getTier().next()
                .<Text>map(next -> guildPlayer.getWorthiness() < next.getRequiredWorthiness()
                        ? Text.translatable("message.taleofkingdoms.quest.objective.worthiness",
                                guildPlayer.getWorthiness(), next.getRequiredWorthiness())
                        : kingdom.hasCompletedTier(kingdom.getTier())
                                ? Text.translatable("message.taleofkingdoms.quest.objective.upgrade_castle", next.getName())
                                : Text.translatable("message.taleofkingdoms.kingdom.required_buildings"))
                .orElseGet(() -> Text.translatable("message.taleofkingdoms.quest.objective.kingdom_complete"));
    }
}
