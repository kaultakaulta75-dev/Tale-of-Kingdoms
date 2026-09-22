package com.convallyria.taleofkingdoms.common.world.guild;

import com.convallyria.taleofkingdoms.common.kingdom.PlayerKingdom;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class GuildPlayer {

    public static final Codec<GuildPlayer> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.BOOL.fieldOf("signed_contract").forGetter(GuildPlayer::hasSignedContract),
                    Codec.INT.fieldOf("coins").forGetter(GuildPlayer::getCoins),
                    Codec.INT.fieldOf("banker_coins").forGetter(GuildPlayer::getBankerCoins),
                    Codec.INT.fieldOf("worthiness").forGetter(GuildPlayer::getWorthiness),
                    Codec.LONG.fieldOf("farmer_last_bread").forGetter(GuildPlayer::getFarmerLastBread),
                    PlayerKingdom.CODEC.optionalFieldOf("kingdom").forGetter(guildPlayer -> Optional.ofNullable(guildPlayer.getKingdom())),
                    Uuids.CODEC.listOf().fieldOf("hunters").forGetter(GuildPlayer::getHunters),
                    Codec.BOOL.fieldOf("has_rebuilt_guild").forGetter(GuildPlayer::hasRebuiltGuild)
            ).apply(instance, GuildPlayer::new)
    );

    private volatile boolean signedContract;
    private volatile int coins;
    private volatile int bankerCoins;
    private volatile int worthiness;
    private volatile long farmerLastBread;
    private volatile @Nullable PlayerKingdom kingdom;
    private final List<UUID> hunters;
    private volatile boolean hasRebuiltGuild;

    public GuildPlayer() {
        this.hunters = new CopyOnWriteArrayList<>();
        this.farmerLastBread = -1;
    }

    private GuildPlayer(boolean signedContract, int coins, int bankerCoins, int worthiness, long farmerLastBread, Optional<PlayerKingdom> kingdom, List<UUID> hunters, boolean hasRebuiltGuild) {
        this.signedContract = signedContract;
        this.coins = Math.max(0, coins);
        this.bankerCoins = Math.max(0, bankerCoins);
        this.worthiness = worthiness;
        this.farmerLastBread = farmerLastBread;
        this.kingdom = kingdom.orElse(null);
        this.hunters = new CopyOnWriteArrayList<>(hunters);
        this.hasRebuiltGuild = hasRebuiltGuild;
    }

    public boolean hasSignedContract() {
        return signedContract;
    }

    public void setSignedContract(boolean signedContract) {
        this.signedContract = signedContract;
    }

    public int getCoins() {
        return coins;
    }

    public synchronized void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public int getBankerCoins() {
        return bankerCoins;
    }

    public synchronized void setBankerCoins(int bankerCoins) {
        this.bankerCoins = Math.max(0, bankerCoins);
    }

    public synchronized boolean trySpendCoins(int amount) {
        if (amount <= 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    public synchronized boolean tryCreditCoins(int amount) {
        if (amount <= 0 || (long) coins + amount > Integer.MAX_VALUE) return false;
        coins += amount;
        return true;
    }

    public synchronized boolean tryDepositCoins(int amount) {
        if (amount <= 0 || coins < amount || (long) bankerCoins + amount > Integer.MAX_VALUE) return false;
        coins -= amount;
        bankerCoins += amount;
        return true;
    }

    public synchronized boolean tryWithdrawCoins(int amount) {
        if (amount <= 0 || bankerCoins < amount || (long) coins + amount > Integer.MAX_VALUE) return false;
        bankerCoins -= amount;
        coins += amount;
        return true;
    }

    public int getWorthiness() {
        return worthiness;
    }

    public void setWorthiness(int worthiness) {
        if (!hasSignedContract()) return;
        this.worthiness = Math.max(0, worthiness);
    }

    public synchronized int addWorthiness(int amount) {
        if (!hasSignedContract() || amount <= 0) return 0;
        int previous = this.worthiness;
        this.worthiness = (int) Math.min(Integer.MAX_VALUE, (long) this.worthiness + amount);
        return this.worthiness - previous;
    }

    public long getFarmerLastBread() {
        return farmerLastBread;
    }

    public void setFarmerLastBread(long farmerLastBread) {
        this.farmerLastBread = farmerLastBread;
    }

    public @Nullable PlayerKingdom getKingdom() {
        return kingdom;
    }

    public void setKingdom(@Nullable PlayerKingdom kingdom) {
        this.kingdom = kingdom;
    }

    public List<UUID> getHunters() {
        return hunters;
    }

    public boolean hasRebuiltGuild() {
        return hasRebuiltGuild;
    }

    public void setHasRebuiltGuild(boolean hasRebuiltGuild) {
        this.hasRebuiltGuild = hasRebuiltGuild;
    }
}
