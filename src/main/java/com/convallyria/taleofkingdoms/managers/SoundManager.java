package com.convallyria.taleofkingdoms.managers;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.EnumMap;
import java.util.Map;

public class SoundManager implements IManager {

    private static final Map<TOKSound, SoundEvent> EVENTS = new EnumMap<>(TOKSound.class);

    static {
        for (TOKSound sound : TOKSound.values()) {
            Identifier identifier = Identifier.of(TaleOfKingdoms.MODID, sound.getPath());
            EVENTS.put(sound, SoundEvent.of(identifier));
        }
    }

    public SoundManager(TaleOfKingdoms tok) {
        TaleOfKingdoms.LOGGER.info("Loading sounds...");
    }

    public SoundEvent getSound(TOKSound sound) {
        return EVENTS.get(sound);
    }

    @Override
    public String getName() {
        return "Sound Manager";
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.SOUND_EVENT.getKey(), helper -> EVENTS.forEach((name, sound) -> {
            TaleOfKingdoms.LOGGER.info("Loading sound: {}", name.getPath());
            helper.register(Identifier.of(TaleOfKingdoms.MODID, name.getPath()), sound);
        }));
    }

    public enum TOKSound {
        TOKTHEME("toktheme");

        private final String path;

        TOKSound(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
}
