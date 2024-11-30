package com.bread.vehiclemod.network.handlers;

import com.bread.vehiclemod.network.MessageWheelState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientMessageHandler
{
    public static void handleWheelStateMessage(MessageWheelState msg, Supplier<NetworkEvent.Context> ctx)
    {
    }
}
