package com.dawnestofbread.vehiclemod.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage<T>
{
    void encode(T message, FriendlyByteBuf buffer);


    T decode(FriendlyByteBuf buffer);

    void handle(T message, Supplier<NetworkEvent.Context> supplier);

    static void enqueueTask(Supplier<NetworkEvent.Context> supplier, Runnable runnable)
    {
        supplier.get().enqueueWork(runnable);
        supplier.get().setPacketHandled(true);
    }
}
