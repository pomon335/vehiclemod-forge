package com.dawnestofbread.vehiclemod.network;

import com.dawnestofbread.vehiclemod.network.handlers.ServerMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageSprint implements IMessage<MessageSprint> {
	private float input;

	public MessageSprint() {}

	public MessageSprint(float input)
	{
		this.input = input;
	}

	@Override
	public void encode(MessageSprint message, FriendlyByteBuf buffer)
	{
		buffer.writeFloat(message.input);
	}

	@Override
	public MessageSprint decode(FriendlyByteBuf buffer)
	{
		return new MessageSprint(buffer.readFloat());
	}

	@Override
	public void handle(MessageSprint message, Supplier<NetworkEvent.Context> supplier)
	{
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ServerMessageHandler.handleSprintMessage(player, message);
			}
		});
		supplier.get().setPacketHandled(true);
	}

	public float getSprint()
	{
		return this.input;
	}
}
