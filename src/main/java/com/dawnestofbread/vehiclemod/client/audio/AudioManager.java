package com.dawnestofbread.vehiclemod.client.audio;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.WeakHashMap;

public class AudioManager {
    public enum SoundType
    {
        ENGINE_MOVING,
        ENGINE_IDLE
    }
    public static SimpleEngineSound playEngineSound(WeakHashMap<AbstractVehicle, EnumMap<SoundType, SimpleEngineSound>> SOUND_MANAGER, AbstractVehicle vehicle, SoundType soundType, SoundEvent soundEvent) {
        Map<SoundType, SimpleEngineSound> soundMap = SOUND_MANAGER.computeIfAbsent(vehicle, v -> new EnumMap<>(SoundType.class));
        SimpleEngineSound sound = soundMap.get(soundType);
        if(sound == null || sound.isStopped() || !Minecraft.getInstance().getSoundManager().isActive(sound))
        {
            sound = new SimpleEngineSound(Minecraft.getInstance().player, vehicle, soundEvent, soundType);
            soundMap.put(soundType, sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
        return sound;
    }

    public static double calculateVolume(double rpm, double minRange, double maxRange) {
        if (rpm < minRange || rpm > maxRange) return 0.0f;
        if (rpm < (minRange + maxRange) / 2) {
            // Fade in
            return (rpm - minRange) / ((maxRange - minRange) / 2);
        } else {
            // Fade out
            return 1.0f - (rpm - (minRange + maxRange) / 2) / ((maxRange - minRange) / 2);
        }
    }
}
