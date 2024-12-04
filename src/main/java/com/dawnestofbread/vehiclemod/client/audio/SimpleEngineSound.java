package com.dawnestofbread.vehiclemod.client.audio;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class SimpleEngineSound extends AbstractTickableSoundInstance {
    private final WeakReference<Player> playerRef;
    private final WeakReference<AbstractVehicle> vehicleRef;

    public SimpleEngineSound(Player player, AbstractVehicle vehicle, SoundEvent soundEvent, AudioManager.SoundType soundType)
    {
        super(soundEvent, SoundSource.NEUTRAL, RandomSource.create());
        this.playerRef = new WeakReference<>(player);
        this.vehicleRef = new WeakReference<>(vehicle);
        this.volume = 0.0F;
        this.pitch = 0.5F;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }

    public SimpleEngineSound setVolume(double volume) {
        this.volume = (float) volume;
        return this;
    }
    public SimpleEngineSound setPitch(double pitch) {
        this.pitch = (float) pitch;
        return this;
    }

    @Override
    public void tick()
    {
        if(this.isStopped())
            return;

        AbstractVehicle vehicle = this.vehicleRef.get();
        Player player = this.playerRef.get();
        if(vehicle == null || player == null || vehicle.SeatManager.get(0).equals(UUID.fromString("00000000-0000-0000-0000-000000000000")) || !vehicle.isAlive())
        {
            this.stop();
            return;
        }

        this.attenuation = vehicle.equals(player.getVehicle()) ? Attenuation.NONE : Attenuation.LINEAR;

//        if(!vehicle.equals(player.getVehicle()))
//        {
//            this.x = (float) (vehicle.getX() + (player.getX() - vehicle.getX()) * 0.65);
//            this.y = (float) (vehicle.getY() + (player.getY() - vehicle.getY()) * 0.65);
//            this.z = (float) (vehicle.getZ() + (player.getZ() - vehicle.getZ()) * 0.65);
//        }
//        else
//        {
//            this.x = vehicle.getX();
//            this.y = vehicle.getY();
//            this.z = vehicle.getZ();
//        }
        this.x = vehicle.getX();
        this.y = vehicle.getY();
        this.z = vehicle.getZ();
    }
}
