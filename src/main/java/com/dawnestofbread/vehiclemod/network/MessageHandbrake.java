package com.dawnestofbread.vehiclemod.network;

import com.dawnestofbread.vehiclemod.network.handlers.ServerMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageHandbrake implements IMessage<MessageHandbrake> {
	private float input;

	public MessageHandbrake() {}

	public MessageHandbrake(float input)
	{
		this.input = input;
	}

	@Override
	public void encode(MessageHandbrake message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.input);
	}

	@Override
	public MessageHandbrake decode(FriendlyByteBuf buffer)
	{
		return new MessageHandbrake(buffer.readFloat());
	}

	@Override
	public void handle(MessageHandbrake message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerMessageHandler.handleHandbrakeMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getHandbrake()
	{
		return this.input;
	}
}
