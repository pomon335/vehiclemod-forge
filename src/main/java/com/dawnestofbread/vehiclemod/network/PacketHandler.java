package com.dawnestofbread.vehiclemod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("vehiclemod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static <T> void registerMessage(Class<T> tClass, IMessage<T> message) {
        INSTANCE.registerMessage(id++, tClass, message::encode, message::decode, message::handle );
    }

    public static void registerAllMessages() {
        registerMessage(MessageThrottle.class, new MessageThrottle());
        registerMessage(MessageSteering.class, new MessageSteering());
        registerMessage(MessageHandbrake.class, new MessageHandbrake());
    }
}
