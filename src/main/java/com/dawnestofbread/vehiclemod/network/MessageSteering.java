package com.dawnestofbread.vehiclemod.network;

import com.dawnestofbread.vehiclemod.network.handlers.ServerMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSteering implements IMessage<MessageSteering> {
	private float input;

	public MessageSteering() {}

	public MessageSteering(float input)
	{
		this.input = input;
	}

	@Override
	public void encode(MessageSteering message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.input);
	}

	@Override
	public MessageSteering decode(FriendlyByteBuf buffer)
	{
		return new MessageSteering(buffer.readFloat());
	}

	@Override
	public void handle(MessageSteering message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerMessageHandler.handleSteeringMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getSteering()
	{
		return this.input;
	}
}
