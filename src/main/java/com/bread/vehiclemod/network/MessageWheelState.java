package com.bread.vehiclemod.network;

import com.bread.vehiclemod.network.handlers.ClientMessageHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWheelState implements IMessage<MessageWheelState> {
	private double angularVelocity;

	public MessageWheelState() {}

	public MessageWheelState(double angVel)
	{
		this.angularVelocity = angVel;
	}

	@Override
	public void encode(MessageWheelState message, FriendlyByteBuf buffer)
	{
		buffer.writeDouble(message.angularVelocity);
	}

	@Override
	public MessageWheelState decode(FriendlyByteBuf buffer)
	{
		return new MessageWheelState(buffer.readFloat());
	}

	@Override
	public void handle(MessageWheelState msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() ->
				// Make sure it's only executed on the physical client
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMessageHandler.handleWheelStateMessage(msg, ctx))
		);
		ctx.get().setPacketHandled(true);
	}
}
