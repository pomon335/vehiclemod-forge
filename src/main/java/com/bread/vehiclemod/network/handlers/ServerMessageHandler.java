package com.bread.vehiclemod.network.handlers;

import com.bread.vehiclemod.AbstractVehicle;
import com.bread.vehiclemod.network.MessageHandbrake;
import com.bread.vehiclemod.network.MessageSteering;
import com.bread.vehiclemod.network.MessageThrottle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ServerMessageHandler
{
    public static void handleThrottleMessage(ServerPlayer player, MessageThrottle message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof AbstractVehicle)
        {
            ((AbstractVehicle) riding).setThrottle(message.getPower());
        }
    }
    public static void handleSteeringMessage(ServerPlayer player, MessageSteering message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof AbstractVehicle)
        {
            ((AbstractVehicle) riding).setSteering(message.getSteering());
        }
    }
    public static void handleHandbrakeMessage(ServerPlayer player, MessageHandbrake message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof AbstractVehicle)
        {
            ((AbstractVehicle) riding).setHandbrake(message.getHandbrake());
        }
    }
}
