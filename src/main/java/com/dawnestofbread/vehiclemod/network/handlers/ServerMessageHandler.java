package com.dawnestofbread.vehiclemod.network.handlers;

import com.dawnestofbread.vehiclemod.AbstractVehicle;
import com.dawnestofbread.vehiclemod.network.MessageHandbrake;
import com.dawnestofbread.vehiclemod.network.MessageSprint;
import com.dawnestofbread.vehiclemod.network.MessageSteering;
import com.dawnestofbread.vehiclemod.network.MessageThrottle;
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
    public static void handleSprintMessage(ServerPlayer player, MessageSprint message)
    {
        Entity riding = player.getVehicle();
        if(riding instanceof AbstractVehicle)
        {
            ((AbstractVehicle) riding).setSprint(message.getSprint());
        }
    }
}
